package net.flectone.pulse.sender;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.provider.PacketProvider;

import java.util.UUID;

@Singleton
public class PacketSender {

    private final PacketEventsAPI<?> packetEvents = PacketEvents.getAPI();
    private final PacketProvider packetProvider;

    @Inject
    public PacketSender(PacketProvider packetProvider) {
        this.packetProvider = packetProvider;
    }

    public void send(UUID uuid, PacketWrapper<?> packetWrapper) {
        Object channel = packetProvider.getChannel(uuid);
        if (channel == null) return;

        packetEvents.getProtocolManager().sendPacket(channel, packetWrapper);
    }

    public void send(FPlayer fPlayer, PacketWrapper<?> packetWrapper) {
        send(fPlayer.getUuid(), packetWrapper);
    }

    public void send(Object channel, PacketWrapper<?> packetWrapper) {
        packetEvents.getProtocolManager().sendPacket(channel, packetWrapper);
    }

    public static void staticSend(FPlayer fPlayer, PacketWrapper<?> packetWrapper) {
        Object channel = PacketEvents.getAPI().getProtocolManager().getChannel(fPlayer.getUuid());
        if (channel == null) return;

        PacketEvents.getAPI().getProtocolManager().sendPacket(channel, packetWrapper);
    }
}
