package net.flectone.pulse.platform.sender;

import com.github.retrooper.packetevents.manager.protocol.ProtocolManager;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.platform.provider.MinecraftPacketProvider;

import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MinecraftPacketSender {

    private final FileFacade fileFacade;
    private final MinecraftPacketProvider packetProvider;

    public void send(Object channel, PacketWrapper<?> packetWrapper, boolean silent) {
        ProtocolManager protocolManager = packetProvider.getApi().getProtocolManager();
        if (silent || fileFacade.config().internal().alwaysSendSilentPacket()) {
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
        send(fPlayer.uuid(), packetWrapper, silent);
    }

    public void send(UUID uuid, PacketWrapper<?> packetWrapper) {
        send(uuid, packetWrapper, false);
    }

    public void send(FPlayer fPlayer, PacketWrapper<?> packetWrapper) {
        send(fPlayer.uuid(), packetWrapper, false);
    }

    public void send(PacketWrapper<?> packetWrapper) {
        send(packetWrapper, false);
    }

    public void send(PacketWrapper<?> packetWrapper, boolean silent) {
        packetProvider.getApi().getProtocolManager()
                .getUsers()
                .forEach(user -> {
                    if (silent || fileFacade.config().internal().alwaysSendSilentPacket()) {
                        user.sendPacketSilently(packetWrapper);
                    } else {
                        user.sendPacket(packetWrapper);
                    }
                });
    }
}
