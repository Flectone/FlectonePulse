package net.flectone.pulse;

import net.flectone.pulse.listener.BungeecordLoginStateListener;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.util.constant.LoginStatus;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.logging.FLogger;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class BungeecordFlectonePulse extends Plugin implements Listener {

    private static final String UNKNOWN_SERVER_NAME = "Unknown";
    private static final String CHANNEL = "BungeeCord";

    private final Set<UUID> pendingConnections = Collections.synchronizedSet(new HashSet<>());
    private final BungeecordLoginStateListener bungeeDisconnectListener = new BungeecordLoginStateListener();

    private FLogger fLogger;

    @Override
    public void onEnable() {
        fLogger = new FLogger(logRecord -> this.getLogger().log(logRecord), () -> null);

        fLogger.logEnabling();

        getProxy().registerChannel(CHANNEL);
        getProxy().getPluginManager().registerListener(this, this);
        getProxy().getPluginManager().registerListener(this, bungeeDisconnectListener);

        fLogger.logEnabled();
    }

    @Override
    public void onDisable() {
        fLogger.logDisabling();

        getProxy().unregisterChannel(CHANNEL);
        getProxy().getPluginManager().unregisterListener(this);
        getProxy().getPluginManager().unregisterListener(bungeeDisconnectListener);

        fLogger.logDisabled();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerConnectEvent(ServerConnectEvent event) {
        ProxiedPlayer player = event.getPlayer();
        if (player.getServer() != null) {
            sendPlayerConnectedEvent(event.getPlayer().getUniqueId(), false);
            return;
        }

        pendingConnections.add(player.getUniqueId());
    }

    @EventHandler
    public void onPluginMessageEvent(PluginMessageEvent event) {
        if (!event.getTag().equals(CHANNEL)) return;

        ProxySender.send(event.getData(), bytes -> ProxyServer.getInstance().getServers().values().stream()
                .filter(serverInfo -> !serverInfo.getPlayers().isEmpty())
                .forEach(serverInfo -> serverInfo.sendData(CHANNEL, bytes)),
                playerUUID -> {
                    if (pendingConnections.remove(playerUUID)) {
                        sendPlayerConnectedEvent(playerUUID, true);
                    }
                }
        );
    }

    @EventHandler
    public void onDisconnectEvent(PlayerDisconnectEvent event) {
        ProxiedPlayer player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        // clear pending connection
        pendingConnections.remove(playerUUID);

        if (bungeeDisconnectListener.getLoginStatus(playerUUID) == LoginStatus.CONNECTED) {
            Server server = event.getPlayer().getServer();
            String serverName = server != null ? server.getInfo().getName() : UNKNOWN_SERVER_NAME;
            ProxyServer.getInstance().getServers().values().stream()
                    .filter(serverInfo -> !serverInfo.getPlayers().isEmpty())
                    .forEach(serverInfo -> ProxySender.send(
                            ModuleName.PLAYER_DISCONNECTED,
                            outputStream -> {
                                outputStream.writeUTF(playerUUID.toString());
                                outputStream.writeBoolean(serverInfo.getName().equals(serverName));
                            },
                            bytes -> serverInfo.sendData(CHANNEL, bytes)
                    ));
        }
    }

    private void sendPlayerConnectedEvent(UUID playerUUID, boolean firstTime) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(playerUUID);

        String serverName = player != null
                ? player.getServer() != null
                  ? player.getServer().getInfo().getName() : UNKNOWN_SERVER_NAME
                : UNKNOWN_SERVER_NAME;

        ProxyServer.getInstance().getServers().values().stream()
                .filter(serverInfo -> !serverInfo.getPlayers().isEmpty())
                .forEach(serverInfo -> ProxySender.send(
                        ModuleName.PLAYER_CONNECTED,
                        outputStream -> {
                            outputStream.writeUTF(playerUUID.toString());
                            outputStream.writeBoolean(serverInfo.getName().equals(serverName));
                            outputStream.writeBoolean(firstTime);
                        },
                        bytes -> serverInfo.sendData(CHANNEL, bytes)
                ));
    }

}
