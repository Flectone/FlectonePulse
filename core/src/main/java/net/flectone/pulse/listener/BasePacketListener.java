package net.flectone.pulse.listener;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.event.UserDisconnectEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.wrapper.login.server.WrapperLoginServerLoginSuccess;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientSettings;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import net.flectone.pulse.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.command.ban.BanModule;
import net.flectone.pulse.module.command.maintenance.MaintenanceModule;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.status.players.PlayersModule;
import net.flectone.pulse.sender.PacketSender;
import net.flectone.pulse.sender.ProxySender;
import net.flectone.pulse.service.FPlayerService;

import java.util.UUID;

@Singleton
public class BasePacketListener implements PacketListener {

    private final FPlayerService fPlayerService;
    private final PacketSender packetSender;
    private final ProxySender proxySender;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final Provider<IntegrationModule> integrationModuleProvider;
    private final Provider<BanModule> banModuleProvider;
    private final Provider<PlayersModule> playersModuleProvider;
    private final Provider<MaintenanceModule> maintenanceModuleProvider;

    @Inject
    public BasePacketListener(FPlayerService fPlayerService,
                              PacketSender packetSender,
                              ProxySender proxySender,
                              PlatformPlayerAdapter platformPlayerAdapter,
                              Provider<IntegrationModule> integrationModuleProvider,
                              Provider<BanModule> banModuleProvider,
                              Provider<PlayersModule> playersModuleProvider,
                              Provider<MaintenanceModule> maintenanceModuleProvider) {
        this.fPlayerService = fPlayerService;
        this.packetSender = packetSender;
        this.proxySender = proxySender;
        this.platformPlayerAdapter = platformPlayerAdapter;
        this.integrationModuleProvider = integrationModuleProvider;
        this.banModuleProvider = banModuleProvider;
        this.playersModuleProvider = playersModuleProvider;
        this.maintenanceModuleProvider = maintenanceModuleProvider;
    }

    @Override
    public void onUserDisconnect(UserDisconnectEvent event) {
        UUID uuid = event.getUser().getUUID();
        if (uuid == null) return;

        FPlayer fPlayer = fPlayerService.getFPlayer(uuid);

        platformPlayerAdapter.onQuit(fPlayer);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        handleClientSettingsEvent(event);
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.isCancelled()) return;

        handleUserLoginEvent(event);
        handleUserJoinEvent(event);
    }

    public void handleClientSettingsEvent(PacketReceiveEvent event) {
        PacketTypeCommon packetType = event.getPacketType();

        if (packetType != PacketType.Play.Client.CLIENT_SETTINGS
                && packetType != PacketType.Configuration.Client.CLIENT_SETTINGS) return;

        UUID uuid = event.getUser().getUUID();
        if (uuid == null) return;

        FPlayer fPlayer = fPlayerService.getFPlayer(uuid);

        String locale = getLocale(fPlayer, event);

        if (locale.equals(fPlayer.getSettingValue(FPlayer.Setting.LOCALE))) return;
        if (!fPlayer.isUnknown()) {
            fPlayerService.saveOrUpdateSetting(fPlayer, FPlayer.Setting.LOCALE, locale);
            return;
        }

        // first time player joined, wait for it to be added
        updateLocaleLater(uuid, locale);
    }

    public void handleUserJoinEvent(PacketSendEvent event) {
        if (event.getPacketType() != PacketType.Play.Server.JOIN_GAME) return;

        UUID uuid = event.getUser().getUUID();
        if (uuid == null) return;

        FPlayer fPlayer = fPlayerService.getFPlayer(uuid);
        platformPlayerAdapter.onJoin(fPlayer, false);
    }

    public void handleUserLoginEvent(PacketSendEvent event) {
        if (event.getPacketType() != PacketType.Login.Server.LOGIN_SUCCESS) return;

        // if no one was on the server, the cache may be invalid for other servers
        // because FlectonePulse on Proxy cannot send a message for servers that have no player
        if (fPlayerService.getFPlayers().isEmpty() && proxySender.isEnable()) {
            // clears the cache of players who might have left from other servers
            fPlayerService.clear();
        }

        event.setCancelled(true);

        WrapperLoginServerLoginSuccess wrapperLoginServerLoginSuccess = new WrapperLoginServerLoginSuccess(event);
        UserProfile userProfile = wrapperLoginServerLoginSuccess.getUserProfile();
        handleUserLogin(userProfile);
    }

    @Async(delay = 40L)
    public void updateLocaleLater(UUID uuid, String locale) {
        FPlayer newFPlayer = fPlayerService.getFPlayer(uuid);
        fPlayerService.saveOrUpdateSetting(newFPlayer, FPlayer.Setting.LOCALE, locale);
    }

    @Async
    public void handleUserLogin(UserProfile userProfile) {
        UUID uuid = userProfile.getUUID();
        if (uuid == null) return;

        fPlayerService.addFPlayer(uuid, userProfile.getName());

        PlayersModule playersModule = playersModuleProvider.get();
        if (playersModule.isEnable() && playersModule.isKicked(userProfile)) return;

        BanModule banModule = banModuleProvider.get();
        if (banModule.isEnable() && banModule.isKicked(userProfile)) return;

        MaintenanceModule maintenanceModule = maintenanceModuleProvider.get();
        if (maintenanceModule.isEnable() && maintenanceModule.isKicked(userProfile)) return;

        packetSender.send(uuid, new WrapperLoginServerLoginSuccess(userProfile));

        fPlayerService.loadData(uuid);
    }

    private String getLocale(FPlayer fPlayer, PacketReceiveEvent event) {
        String locale = integrationModuleProvider.get().getTritonLocale(fPlayer);
        if (locale == null) {
            WrapperPlayClientSettings wrapperPlayClientSettings = new WrapperPlayClientSettings(event);
            locale = wrapperPlayClientSettings.getLocale();
        }

        return locale;
    }
}
