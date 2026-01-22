package net.flectone.pulse.platform.provider;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FPlayer;
import org.jspecify.annotations.Nullable;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PacketProvider {

    @Getter
    private final PacketEventsAPI<?> api = PacketEvents.getAPI();

    public Object getChannel(UUID uuid) {
        return api.getProtocolManager().getChannel(uuid);
    }

    public User getUser(UUID uuid) {
        Object channel = getChannel(uuid);
        if (channel == null) return null;

        return api.getProtocolManager().getUser(channel);
    }

    public User getUser(FPlayer fPlayer) {
        return getUser(fPlayer.getUuid());
    }

    public int getPing(Object player) {
        return api.getPlayerManager().getPing(player);
    }

    public ServerVersion getServerVersion() {
        return api.getServerManager().getVersion();
    }

    public @Nullable String getHostAddress(UUID uuid) {
        User user = getUser(uuid);
        if (user == null) return null;

        return getHostAddress(user.getAddress());
    }

    public @Nullable String getHostAddress(@Nullable InetSocketAddress inetSocketAddress) {
        if (inetSocketAddress == null) return null;

        InetAddress inetAddress = inetSocketAddress.getAddress();
        if (inetAddress == null) return null;

        return inetAddress.getHostAddress();
    }

    public boolean isNewerThanOrEquals(FPlayer fPlayer, ClientVersion clientVersion) {
        User user = getUser(fPlayer);
        if (user == null) return false;

        return user.getPacketVersion().isNewerThanOrEquals(clientVersion);
    }
}
