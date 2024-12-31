package net.flectone.pulse.listener;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;

public class AbstractPacketListener implements PacketListener {

    protected boolean cancelMessageNotDelivered(PacketSendEvent event, String key) {
        if (!key.equals("multiplayer.message_not_delivered")) return false;
        event.setCancelled(true);
        return true;
    }

}
