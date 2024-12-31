package net.flectone.pulse.module.message.quit.listener;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSystemChatMessage;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.listener.AbstractPacketListener;
import net.flectone.pulse.module.message.quit.QuitModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;

@Singleton
public class QuitPacketListener extends AbstractPacketListener {

    private final QuitModule quitModule;

    @Inject
    public QuitPacketListener(QuitModule quitModule) {
        this.quitModule = quitModule;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.isCancelled()) return;
        if (event.getPacketType() != PacketType.Play.Server.SYSTEM_CHAT_MESSAGE) return;

        WrapperPlayServerSystemChatMessage wrapper = new WrapperPlayServerSystemChatMessage(event);
        Component component = wrapper.getMessage();
        if (!(component instanceof TranslatableComponent translatableComponent)) return;

        String key = translatableComponent.key();
        if (cancelMessageNotDelivered(event, key)) return;
        if (!key.startsWith("multiplayer.player.left")) return;
        if (!quitModule.isEnable()) return;

        event.setCancelled(true);
    }
}
