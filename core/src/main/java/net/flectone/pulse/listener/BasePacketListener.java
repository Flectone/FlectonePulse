package net.flectone.pulse.listener;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.UserDisconnectEvent;
import com.github.retrooper.packetevents.event.UserLoginEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientSettings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.ThreadManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.command.mail.MailModule;
import net.flectone.pulse.module.message.bubble.manager.BubbleManager;
import net.flectone.pulse.module.message.greeting.GreetingModule;
import net.flectone.pulse.module.message.join.JoinModule;
import net.flectone.pulse.module.message.quit.QuitModule;

import java.util.UUID;

@Singleton
public class BasePacketListener extends AbstractPacketListener {

    private final FPlayerManager fPlayerManager;
    private final ThreadManager threadManager;

    @Inject private QuitModule quitModule;
    @Inject private JoinModule joinModule;
    @Inject private GreetingModule greetingModule;
    @Inject private MailModule mailModule;
    @Inject private BubbleManager bubbleManager;

    @Inject
    public BasePacketListener(FPlayerManager fPlayerManager,
                              ThreadManager threadManager) {
        this.fPlayerManager = fPlayerManager;
        this.threadManager = threadManager;
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

        threadManager.runDatabase(database -> {
            FPlayer fPlayer = fPlayerManager.put(database, uuid, entityId, name, ip);

            joinModule.send(fPlayer, true);
            greetingModule.send(fPlayer);
            mailModule.send(fPlayer);
        });
    }

    @Override
    public void onUserDisconnect(UserDisconnectEvent event) {
        if (event.getUser().getUUID() == null) return;

        threadManager.runDatabase(database -> {
            FPlayer fPlayer = fPlayerManager.get(event.getUser().getUUID());
            if (!fPlayer.isOnline()) return;

            fPlayerManager.remove(database, fPlayer);
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

        WrapperPlayClientSettings wrapperPlayClientSettings = new WrapperPlayClientSettings(event);

        String locale = wrapperPlayClientSettings.getLocale();

        if (locale.equals(fPlayer.getLocale())) return;
        if (!fPlayer.isUnknown()) {
            setLocale(fPlayer, locale);
            return;
        }

        // first time player joined, wait for it to be added
        // this needs to change in the future
        threadManager.runAsyncLater(() -> {
            FPlayer newFPlayer = fPlayerManager.get(uuid);

            setLocale(newFPlayer, locale);
        }, 20);
    }

    private void setLocale(FPlayer fPlayer, String locale) {
        fPlayer.setLocale(locale);
        threadManager.runDatabase(database -> database.updateFPlayer(fPlayer));
    }
}
