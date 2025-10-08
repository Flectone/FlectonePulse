package net.flectone.pulse.platform.provider;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.event.EventManager;
import com.github.retrooper.packetevents.manager.player.PlayerManager;
import com.github.retrooper.packetevents.manager.protocol.ProtocolManager;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.model.entity.FPlayer;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.UUID;

@Getter
@Singleton
public class PacketProvider {

    private final PacketEventsAPI<?> api = PacketEvents.getAPI();

    @Inject
    public PacketProvider() {
    }

    public EventManager getEventManager() {
        return api.getEventManager();
    }

    public ProtocolManager getProtocolManager() {
        return api.getProtocolManager();
    }

    public PlayerManager getPlayerManager() {
        return api.getPlayerManager();
    }

    public Object getChannel(UUID uuid) {
        return getProtocolManager().getChannel(uuid);
    }

    public User getUser(UUID uuid) {
        Object channel = getChannel(uuid);
        if (channel == null) return null;

        return getProtocolManager().getUser(channel);
    }

    public User getUser(FPlayer fPlayer) {
        return getUser(fPlayer.getUuid());
    }

    public int getPing(Object player) {
        return getPlayerManager().getPing(player);
    }

    public ServerVersion getServerVersion() {
        return api.getServerManager().getVersion();
    }

    public boolean isNewerThanOrEquals(FPlayer fPlayer, ClientVersion clientVersion) {
        User user = getUser(fPlayer);
        if (user == null) return false;

        return user.getClientVersion().isNewerThanOrEquals(clientVersion);
    }

    public @Nullable String getHostAddress(@Nullable InetSocketAddress inetSocketAddress) {
        if (inetSocketAddress == null) return null;

        InetAddress inetAddress = inetSocketAddress.getAddress();
        if (inetAddress == null) return null;

        return inetAddress.getHostAddress();
    }
}
