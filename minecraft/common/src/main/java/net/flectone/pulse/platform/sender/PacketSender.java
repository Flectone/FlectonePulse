package net.flectone.pulse.platform.sender;

import com.github.retrooper.packetevents.manager.protocol.ProtocolManager;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.platform.provider.PacketProvider;

import java.util.UUID;

/**
 * Sends network packets to players with silent option support.
 *
 * <p><b>Usage example:</b>
 * <pre>{@code
 * PacketSender packetSender = flectonePulse.get(PacketSender.class);
 *
 * // Send packet silently
 * packetSender.send(player, packet, true);
 *
 * // Broadcast packet to all players
 * packetSender.send(packet);
 * }</pre>
 *
 * @since 0.8.0
 * @author TheFaser
 */
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PacketSender {

    private final PacketProvider packetProvider;

    /**
     * Sends a packet through a network channel.
     *
     * @param channel the network channel to send through
     * @param packetWrapper the packet to send
     * @param silent whether to send silently
     */
    public void send(Object channel, PacketWrapper<?> packetWrapper, boolean silent) {
        ProtocolManager protocolManager = packetProvider.getApi().getProtocolManager();
        if (silent) {
            protocolManager.sendPacketSilently(channel, packetWrapper);
        } else {
            protocolManager.sendPacket(channel, packetWrapper);
        }
    }

    /**
     * Sends a packet to a player by UUID.
     *
     * @param uuid the player's UUID
     * @param packetWrapper the packet to send
     * @param silent whether to send silently
     */
    public void send(UUID uuid, PacketWrapper<?> packetWrapper, boolean silent) {
        Object channel = packetProvider.getChannel(uuid);
        if (channel == null) return;

        send(channel, packetWrapper, silent);
    }

    /**
     * Sends a packet to a player.
     *
     * @param fPlayer the player to receive the packet
     * @param packetWrapper the packet to send
     * @param silent whether to send silently
     */
    public void send(FPlayer fPlayer, PacketWrapper<?> packetWrapper, boolean silent) {
        send(fPlayer.uuid(), packetWrapper, silent);
    }

    /**
     * Sends a packet to a player by UUID (not silent).
     *
     * @param uuid the player's UUID
     * @param packetWrapper the packet to send
     */
    public void send(UUID uuid, PacketWrapper<?> packetWrapper) {
        send(uuid, packetWrapper, false);
    }

    /**
     * Sends a packet to a player (not silent).
     *
     * @param fPlayer the player to receive the packet
     * @param packetWrapper the packet to send
     */
    public void send(FPlayer fPlayer, PacketWrapper<?> packetWrapper) {
        send(fPlayer.uuid(), packetWrapper, false);
    }

    /**
     * Broadcasts a packet to all online players.
     *
     * @param packetWrapper the packet to broadcast
     */
    public void send(PacketWrapper<?> packetWrapper) {
        packetProvider.getApi().getProtocolManager()
                .getUsers()
                .forEach(user -> user.sendPacket(packetWrapper));
    }
}
