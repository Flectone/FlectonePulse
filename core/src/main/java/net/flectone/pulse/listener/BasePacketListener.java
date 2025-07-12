package net.flectone.pulse.listener;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.wrapper.login.server.WrapperLoginServerLoginSuccess;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientSettings;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChatMessage;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSystemChatMessage;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.event.message.TranslatableMessageEvent;
import net.flectone.pulse.module.command.ban.BanModule;
import net.flectone.pulse.module.command.maintenance.MaintenanceModule;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.status.players.PlayersModule;
import net.flectone.pulse.registry.EventProcessRegistry;
import net.flectone.pulse.sender.PacketSender;
import net.flectone.pulse.sender.ProxySender;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.MinecraftTranslationKeys;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.UUID;

@Singleton
public class BasePacketListener implements PacketListener {

    private final FPlayerService fPlayerService;
    private final PacketSender packetSender;
    private final ProxySender proxySender;
    private final Provider<IntegrationModule> integrationModuleProvider;
    private final Provider<BanModule> banModuleProvider;
    private final Provider<PlayersModule> playersModuleProvider;
    private final Provider<MaintenanceModule> maintenanceModuleProvider;
    private final EventProcessRegistry eventProcessRegistry;

    @Inject
    public BasePacketListener(FPlayerService fPlayerService,
                              PacketSender packetSender,
                              ProxySender proxySender,
                              Provider<IntegrationModule> integrationModuleProvider,
                              Provider<BanModule> banModuleProvider,
                              Provider<PlayersModule> playersModuleProvider,
                              Provider<MaintenanceModule> maintenanceModuleProvider,
                              EventProcessRegistry eventProcessRegistry) {
        this.fPlayerService = fPlayerService;
        this.packetSender = packetSender;
        this.proxySender = proxySender;
        this.integrationModuleProvider = integrationModuleProvider;
        this.banModuleProvider = banModuleProvider;
        this.playersModuleProvider = playersModuleProvider;
        this.maintenanceModuleProvider = maintenanceModuleProvider;
        this.eventProcessRegistry = eventProcessRegistry;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        handleClientSettingsEvent(event);
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.isCancelled()) return;

        handleUserLoginEvent(event);
        handleMessageEvent(event);
    }

    public void handleMessageEvent(PacketSendEvent event) {
        TranslatableComponent translatableComponent = parseTranslatableComponent(event);
        if (translatableComponent == null) return;

        MinecraftTranslationKeys key = MinecraftTranslationKeys.fromString(translatableComponent.key());

        // skip minecraft warning
        if (key == MinecraftTranslationKeys.MULTIPLAYER_MESSAGE_NOT_DELIVERED) {
            event.setCancelled(true);
            return;
        }

        eventProcessRegistry.processEvent(new TranslatableMessageEvent(key, translatableComponent, event));
    }

    private TranslatableComponent parseTranslatableComponent(PacketSendEvent event) {
        Component component = null;

        if (event.getPacketType() == PacketType.Play.Server.CHAT_MESSAGE) {
            WrapperPlayServerChatMessage wrapper = new WrapperPlayServerChatMessage(event);
            component = wrapper.getMessage().getChatContent();
        } else if (event.getPacketType() == PacketType.Play.Server.SYSTEM_CHAT_MESSAGE) {
            WrapperPlayServerSystemChatMessage wrapper = new WrapperPlayServerSystemChatMessage(event);
            component = wrapper.getMessage();
        }

        if (component instanceof TranslatableComponent translatableComponent) {
            return translatableComponent;
        }

        return null;
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
