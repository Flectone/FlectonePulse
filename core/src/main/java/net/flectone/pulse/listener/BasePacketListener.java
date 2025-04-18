package net.flectone.pulse.listener;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.event.UserDisconnectEvent;
import com.github.retrooper.packetevents.event.UserLoginEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.wrapper.login.server.WrapperLoginServerLoginSuccess;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientSettings;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.command.ban.BanModule;
import net.flectone.pulse.module.command.mail.MailModule;
import net.flectone.pulse.module.command.maintenance.MaintenanceModule;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.bubble.service.BubbleService;
import net.flectone.pulse.module.message.greeting.GreetingModule;
import net.flectone.pulse.module.message.join.JoinModule;
import net.flectone.pulse.module.message.quit.QuitModule;
import net.flectone.pulse.module.message.status.players.PlayersModule;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.sender.PacketSender;
import net.flectone.pulse.service.FPlayerService;

import java.util.UUID;

@Singleton
public class BasePacketListener extends AbstractPacketListener {

    private final FPlayerService fPlayerService;
    private final TaskScheduler taskScheduler;
    private final PacketSender packetSender;
    private final Provider<QuitModule> quitModuleProvider;
    private final Provider<JoinModule> joinModuleProvider;
    private final Provider<GreetingModule> greetingModuleProvider;
    private final Provider<MailModule> mailModuleProvider;
    private final Provider<IntegrationModule> integrationModuleProvider;
    private final Provider<BubbleService> bubbleServiceProvider;
    private final Provider<BanModule> banModuleProvider;
    private final Provider<PlayersModule> playersModuleProvider;
    private final Provider<MaintenanceModule> maintenanceModuleProvider;

    @Inject
    public BasePacketListener(FPlayerService fPlayerService,
                              TaskScheduler taskScheduler,
                              PacketSender packetSender,
                              Provider<QuitModule> quitModuleProvider,
                              Provider<JoinModule> joinModuleProvider,
                              Provider<GreetingModule> greetingModuleProvider,
                              Provider<MailModule> mailModuleProvider,
                              Provider<IntegrationModule> integrationModuleProvider,
                              Provider<BubbleService> bubbleServiceProvider,
                              Provider<BanModule> banModuleProvider,
                              Provider<PlayersModule> playersModuleProvider,
                              Provider<MaintenanceModule> maintenanceModuleProvider) {
        this.fPlayerService = fPlayerService;
        this.taskScheduler = taskScheduler;
        this.packetSender = packetSender;
        this.quitModuleProvider = quitModuleProvider;
        this.joinModuleProvider = joinModuleProvider;
        this.greetingModuleProvider = greetingModuleProvider;
        this.mailModuleProvider = mailModuleProvider;
        this.integrationModuleProvider = integrationModuleProvider;
        this.bubbleServiceProvider = bubbleServiceProvider;
        this.banModuleProvider = banModuleProvider;
        this.playersModuleProvider = playersModuleProvider;
        this.maintenanceModuleProvider = maintenanceModuleProvider;
    }

    @Override
    public void onUserLogin(UserLoginEvent event) {
        User user = event.getUser();
        if (user == null) return;

        UUID uuid = user.getUUID();
        if (uuid == null) return;

        String name = user.getName();

        taskScheduler.runAsync(() -> {
            FPlayer fPlayer = fPlayerService.addAndGetFPlayer(uuid, name);

            joinModuleProvider.get().send(fPlayer, true);
            greetingModuleProvider.get().send(fPlayer);
            mailModuleProvider.get().send(fPlayer);
        });
    }

    @Override
    public void onUserDisconnect(UserDisconnectEvent event) {
        if (event.getUser().getUUID() == null) return;

        taskScheduler.runAsync(() -> {
            FPlayer fPlayer = fPlayerService.getFPlayer(event.getUser().getUUID());
            if (!fPlayer.isOnline()) return;

            fPlayerService.clearAndSave(fPlayer);
            bubbleServiceProvider.get().clear(fPlayer);
            quitModuleProvider.get().send(fPlayer);
        });
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        PacketTypeCommon packetType = event.getPacketType();

        if (packetType != PacketType.Play.Client.CLIENT_SETTINGS
                && packetType != PacketType.Configuration.Client.CLIENT_SETTINGS) return;

        UUID uuid = event.getUser().getUUID();
        FPlayer fPlayer = fPlayerService.getFPlayer(uuid);

        String locale = getLocale(fPlayer, event);

        if (locale.equals(fPlayer.getSettingValue(FPlayer.Setting.LOCALE))) return;
        if (!fPlayer.isUnknown()) {
            fPlayerService.saveOrUpdateSetting(fPlayer, FPlayer.Setting.LOCALE, locale);
            return;
        }

        // first time player joined, wait for it to be added
        taskScheduler.runAsyncLater(() -> {
            FPlayer newFPlayer = fPlayerService.getFPlayer(uuid);
            fPlayerService.saveOrUpdateSetting(newFPlayer, FPlayer.Setting.LOCALE, locale);
        }, 40);
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() != PacketType.Login.Server.LOGIN_SUCCESS) return;
        if (event.isCancelled()) return;

        PlayersModule playersModule = playersModuleProvider.get();
        BanModule banModule = banModuleProvider.get();
        MaintenanceModule maintenanceModule = maintenanceModuleProvider.get();

        if (!playersModuleProvider.get().isEnable() && !banModuleProvider.get().isEnable() && !maintenanceModuleProvider.get().isEnable()) return;

        event.setCancelled(true);

        WrapperLoginServerLoginSuccess wrapperLoginServerLoginSuccess = new WrapperLoginServerLoginSuccess(event);
        UserProfile userProfile = wrapperLoginServerLoginSuccess.getUserProfile();

        if (playersModule.isEnable() && playersModule.isKicked(userProfile)) return;
        if (banModule.isEnable() && banModule.isKicked(userProfile)) return;
        if (maintenanceModule.isEnable() && maintenanceModule.isKicked(userProfile)) return;

        packetSender.send(userProfile.getUUID(), new WrapperLoginServerLoginSuccess(userProfile));
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
