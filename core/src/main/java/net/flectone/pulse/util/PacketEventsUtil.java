package net.flectone.pulse.util;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.FPlayer;

import java.util.UUID;

@Singleton
public class PacketEventsUtil {

    private final PacketEventsAPI<?> packetEvents = PacketEvents.getAPI();

    @Inject
    public PacketEventsUtil() {

    }

    public User getUser(UUID uuid) {
        Object channel = packetEvents.getProtocolManager().getChannel(uuid);
        if (channel == null) return null;

        return packetEvents.getProtocolManager().getUser(channel);
    }

    public User getUser(FPlayer fPlayer) {
        return getUser(fPlayer.getUuid());
    }

    public void sendPacket(UUID uuid, PacketWrapper<?> packetWrapper) {
        User user = getUser(uuid);
        if (user == null) return;

        user.sendPacket(packetWrapper);
    }

    public void sendPacket(FPlayer fPlayer, PacketWrapper<?> packetWrapper) {
        sendPacket(fPlayer.getUuid(), packetWrapper);
    }

    public void sendPacket(Object channel, PacketWrapper<?> packetWrapper) {
        packetEvents.getProtocolManager().sendPacket(channel, packetWrapper);
    }

    public static void sendStaticPacket(FPlayer fPlayer, PacketWrapper<?> packetWrapper) {
        Object channel = PacketEvents.getAPI().getProtocolManager().getChannel(fPlayer.getUuid());
        if (channel == null) return;

        PacketEvents.getAPI().getProtocolManager().getUser(channel).sendPacket(packetWrapper);
    }
}
