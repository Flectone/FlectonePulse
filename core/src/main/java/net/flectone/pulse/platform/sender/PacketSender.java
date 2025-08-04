package net.flectone.pulse.platform.sender;

import com.github.retrooper.packetevents.manager.protocol.ProtocolManager;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.platform.provider.PacketProvider;

import java.util.UUID;

@Singleton
public class PacketSender {

    private final PacketProvider packetProvider;

    @Inject
    public PacketSender(PacketProvider packetProvider) {
        this.packetProvider = packetProvider;
    }

    public void send(Object channel, PacketWrapper<?> packetWrapper, boolean silent) {
        ProtocolManager protocolManager = packetProvider.getProtocolManager();
        if (silent) {
            protocolManager.sendPacketSilently(channel, packetWrapper);
        } else {
            protocolManager.sendPacket(channel, packetWrapper);
        }
    }

    public void send(UUID uuid, PacketWrapper<?> packetWrapper, boolean silent) {
        Object channel = packetProvider.getChannel(uuid);
        if (channel == null) return;

        send(channel, packetWrapper, silent);
    }

    public void send(FPlayer fPlayer, PacketWrapper<?> packetWrapper, boolean silent) {
        send(fPlayer.getUuid(), packetWrapper, silent);
    }

    public void send(UUID uuid, PacketWrapper<?> packetWrapper) {
        send(uuid, packetWrapper, false);
    }

    public void send(FPlayer fPlayer, PacketWrapper<?> packetWrapper) {
        send(fPlayer.getUuid(), packetWrapper, false);
    }

    public void send(PacketWrapper<?> packetWrapper) {
        packetProvider.getProtocolManager()
                .getUsers()
                .forEach(user -> user.sendPacket(packetWrapper));
    }
}
