package net.flectone.pulse.listener;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChatMessage;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSystemChatMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;

public abstract class AbstractPacketListener implements PacketListener {

    protected boolean cancelMessageNotDelivered(PacketSendEvent event, String key) {
        if (!key.equals("multiplayer.message_not_delivered")) return false;
        event.setCancelled(true);
        return true;
    }

    protected TranslatableComponent getTranslatableComponent(PacketSendEvent event) {
        Component component = null;

        if (event.getPacketType() == PacketType.Play.Server.CHAT_MESSAGE) {
            WrapperPlayServerChatMessage wrapper = new WrapperPlayServerChatMessage(event);
            component = wrapper.getMessage().getChatContent();
        }

        if (event.getPacketType() == PacketType.Play.Server.SYSTEM_CHAT_MESSAGE) {
            WrapperPlayServerSystemChatMessage wrapper = new WrapperPlayServerSystemChatMessage(event);
            component = wrapper.getMessage();
        }

        if (component instanceof TranslatableComponent translatableComponent) {
            return translatableComponent;
        }

        return null;
    }

}
