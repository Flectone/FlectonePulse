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
import com.google.inject.Singleton;
import net.flectone.pulse.database.dao.SettingDAO;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.command.ban.BanModule;
import net.flectone.pulse.module.command.mail.MailModule;
import net.flectone.pulse.module.command.maintenance.MaintenanceModule;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.bubble.manager.BubbleManager;
import net.flectone.pulse.module.message.greeting.GreetingModule;
import net.flectone.pulse.module.message.join.JoinModule;
import net.flectone.pulse.module.message.quit.QuitModule;
import net.flectone.pulse.module.message.status.players.PlayersModule;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.util.PacketEventsUtil;

import java.util.UUID;

@Singleton
public class BasePacketListener extends AbstractPacketListener {

    private final SettingDAO settingDAO;
    private final FPlayerManager fPlayerManager;
    private final TaskScheduler taskScheduler;
    private final PacketEventsUtil packetEventsUtil;

    @Inject private QuitModule quitModule;
    @Inject private JoinModule joinModule;
    @Inject private GreetingModule greetingModule;
    @Inject private MailModule mailModule;
    @Inject private IntegrationModule integrationModule;
    @Inject private BubbleManager bubbleManager;
    @Inject private BanModule banModule;
    @Inject private PlayersModule playersModule;
    @Inject private MaintenanceModule maintenanceModule;

    @Inject
    public BasePacketListener(SettingDAO settingDAO,
                              FPlayerManager fPlayerManager,
                              TaskScheduler taskScheduler,
                              PacketEventsUtil packetEventsUtil) {
        this.settingDAO = settingDAO;
        this.fPlayerManager = fPlayerManager;
        this.taskScheduler = taskScheduler;
        this.packetEventsUtil = packetEventsUtil;
    }

    @Override
    public void onUserLogin(UserLoginEvent event) {
        User user = event.getUser();
        if (user == null) return;

        UUID uuid = user.getUUID();
        if (uuid == null) return;

        int entityId = user.getEntityId();
        String name = user.getName();
        String ip = user.getAddress().getHostString();

        taskScheduler.runAsync(() -> {
            FPlayer fPlayer = fPlayerManager.createAndPut(uuid, entityId, name);

            joinModule.send(fPlayer, true);
            greetingModule.send(fPlayer);
            mailModule.send(fPlayer);
        });
    }

    @Override
    public void onUserDisconnect(UserDisconnectEvent event) {
        if (event.getUser().getUUID() == null) return;

        taskScheduler.runAsync(() -> {
            FPlayer fPlayer = fPlayerManager.get(event.getUser().getUUID());
            if (!fPlayer.isOnline()) return;

            fPlayerManager.saveAndRemove(fPlayer);
            bubbleManager.remove(fPlayer);
            quitModule.send(fPlayer);
        });
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        PacketTypeCommon packetType = event.getPacketType();

        if (packetType != PacketType.Play.Client.CLIENT_SETTINGS
                && packetType != PacketType.Configuration.Client.CLIENT_SETTINGS) return;

        UUID uuid = event.getUser().getUUID();
        FPlayer fPlayer = fPlayerManager.get(uuid);

        String locale = getLocale(fPlayer, event);

        if (locale.equals(fPlayer.getSettingValue(FPlayer.Setting.LOCALE))) return;
        if (!fPlayer.isUnknown()) {
            setLocale(fPlayer, locale);
            return;
        }

        // first time player joined, wait for it to be added
        taskScheduler.runAsyncLater(() -> {
            FPlayer newFPlayer = fPlayerManager.get(uuid);

            setLocale(newFPlayer, locale);
        }, 40);
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() != PacketType.Login.Server.LOGIN_SUCCESS) return;
        if (event.isCancelled()) return;
        if (!playersModule.isEnable() && !banModule.isEnable() && !maintenanceModule.isEnable()) return;

        event.setCancelled(true);

        WrapperLoginServerLoginSuccess wrapperLoginServerLoginSuccess = new WrapperLoginServerLoginSuccess(event);
        UserProfile userProfile = wrapperLoginServerLoginSuccess.getUserProfile();

        if (playersModule.isEnable() && playersModule.isKicked(userProfile)) return;
        if (banModule.isEnable() && banModule.isKicked(userProfile)) return;
        if (maintenanceModule.isEnable() && maintenanceModule.isKicked(userProfile)) return;

        packetEventsUtil.sendPacket(userProfile.getUUID(), new WrapperLoginServerLoginSuccess(userProfile));
    }

    private String getLocale(FPlayer fPlayer, PacketReceiveEvent event) {
        String locale = integrationModule.getTritonLocale(fPlayer);
        if (locale == null) {
            WrapperPlayClientSettings wrapperPlayClientSettings = new WrapperPlayClientSettings(event);
            locale = wrapperPlayClientSettings.getLocale();
        }

        return locale;
    }

    private void setLocale(FPlayer fPlayer, String locale) {
        fPlayer.setSetting(FPlayer.Setting.LOCALE, locale);
        settingDAO.insertOrUpdate(fPlayer, FPlayer.Setting.LOCALE);
    }
}
