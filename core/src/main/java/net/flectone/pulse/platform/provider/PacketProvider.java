package net.flectone.pulse.platform.provider;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.entity.FPlayer;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.UUID;

@Singleton
public class PacketProvider {

    private final PacketEventsAPI<?> packetEvents = PacketEvents.getAPI();

    @Inject
    public PacketProvider() {
    }

    public Object getChannel(UUID uuid) {
        return packetEvents.getProtocolManager().getChannel(uuid);
    }

    public User getUser(UUID uuid) {
        Object channel = getChannel(uuid);
        if (channel == null) return null;

        return packetEvents.getProtocolManager().getUser(channel);
    }

    public User getUser(FPlayer fPlayer) {
        return getUser(fPlayer.getUuid());
    }

    public int getPing(Object player) {
        return packetEvents.getPlayerManager().getPing(player);
    }

    public ServerVersion getServerVersion() {
        return packetEvents.getServerManager().getVersion();
    }

    public @Nullable String getHostAddress(@Nullable InetSocketAddress inetSocketAddress) {
        if (inetSocketAddress == null) return null;

        InetAddress inetAddress = inetSocketAddress.getAddress();
        if (inetAddress == null) return null;

        return inetAddress.getHostAddress();
    }
}
