package net.flectone.pulse;

import com.google.inject.Inject;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import net.flectone.pulse.listener.VelocityLoginStateListener;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.util.constant.LoginStatus;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.logging.FLogger;
import org.slf4j.Logger;

import java.util.*;

@Plugin(
        id = "flectonepulse",
        name = "FlectonePulseVelocity",
        version = BuildConfig.PROJECT_VERSION,
        authors = BuildConfig.PROJECT_AUTHOR,
        description = BuildConfig.PROJECT_DESCRIPTION,
        url = BuildConfig.PROJECT_WEBSITE
)
public class VelocityFlectonePulse {

    private static final MinecraftChannelIdentifier IDENTIFIER = MinecraftChannelIdentifier.from("flectonepulse:main");

    private final Set<UUID> pendingConnections = Collections.synchronizedSet(new HashSet<>());

    private final ProxyServer proxyServer;
    private final FLogger fLogger;
    private final VelocityLoginStateListener velocityLoginStateListener;

    @Inject
    public VelocityFlectonePulse(ProxyServer proxyServer,
                                 VelocityLoginStateListener velocityLoginStateListener,
                                 Logger logger) {
        this.proxyServer = proxyServer;
        this.fLogger = new FLogger(logRecord -> logger.info(logRecord.getMessage()), () -> null);
        this.velocityLoginStateListener = velocityLoginStateListener;
    }

    @Subscribe
    public void onProxyInitializeEvent(ProxyInitializeEvent event) {
        fLogger.logEnabling();

        proxyServer.getChannelRegistrar().register(IDENTIFIER);
        proxyServer.getEventManager().register(this, velocityLoginStateListener);

        fLogger.logEnabled();
    }

    @Subscribe
    public void onProxyShutdownEvent(ProxyShutdownEvent event) {
        fLogger.logDisabling();

        proxyServer.getChannelRegistrar().unregister(IDENTIFIER);
        proxyServer.getEventManager().unregisterListeners(this);

        fLogger.logDisabled();
    }

    @Subscribe(order = PostOrder.LAST)
    public void onServerPreConnectEvent(ServerPreConnectEvent event) {
        if (event.getPreviousServer() != null) return;

        pendingConnections.add(event.getPlayer().getUniqueId());
    }

    @Subscribe
    public void onServerPostConnectEvent(ServerPostConnectEvent event) {
        if (event.getPreviousServer() == null) return;

        sendPlayerConnectedEvent(event.getPlayer().getUniqueId(), false);
    }

    @Subscribe
    public void onPluginMessageEvent(PluginMessageEvent event) {
        if (!event.getIdentifier().equals(IDENTIFIER)) return;

        ProxySender.send(event.getData(), bytes -> proxyServer.getAllServers().stream()
                .filter(registeredServer -> !registeredServer.getPlayersConnected().isEmpty())
                .forEach(registeredServer -> registeredServer.sendPluginMessage(IDENTIFIER, bytes)),
                playerUUID -> {
                    if (pendingConnections.remove(playerUUID)) {
                        sendPlayerConnectedEvent(playerUUID, true);
                    }
                }
        );

        event.setResult(PluginMessageEvent.ForwardResult.handled());
    }

    @Subscribe
    public void onDisconnectEvent(DisconnectEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();

        // clear pending connection
        pendingConnections.remove(playerUUID);

        if (velocityLoginStateListener.getLoginStatus(playerUUID) == LoginStatus.CONNECTED) {
            Optional<ServerConnection> serverConnection = event.getPlayer().getCurrentServer();
            if (serverConnection.isEmpty()) return;

            String serverName = serverConnection.get().getServerInfo().getName();
            proxyServer.getAllServers().stream()
                    .filter(registeredServer -> !registeredServer.getPlayersConnected().isEmpty())
                    .forEach(registeredServer -> ProxySender.send(
                            ModuleName.PLAYER_DISCONNECTED,
                            outputStream -> {
                                outputStream.writeUTF(playerUUID.toString());
                                outputStream.writeBoolean(registeredServer.getServerInfo().getName().equals(serverName));
                            },
                            bytes -> registeredServer.sendPluginMessage(IDENTIFIER, bytes)
                    ));
        }
    }

    private void sendPlayerConnectedEvent(UUID playerUUID, boolean firstTime) {
        Optional<Player> player = proxyServer.getPlayer(playerUUID);
        if (player.isEmpty()) return;

        Optional<ServerConnection> serverConnection = player.get().getCurrentServer();
        if (serverConnection.isEmpty()) return;

        String serverName = serverConnection.get().getServerInfo().getName();

        proxyServer.getAllServers().stream()
                .filter(registeredServer -> !registeredServer.getPlayersConnected().isEmpty())
                .forEach(registeredServer -> ProxySender.send(
                        ModuleName.PLAYER_CONNECTED,
                        outputStream -> {
                            outputStream.writeUTF(playerUUID.toString());
                            outputStream.writeBoolean(registeredServer.getServerInfo().getName().equals(serverName));
                            outputStream.writeBoolean(firstTime);
                        },
                        bytes -> registeredServer.sendPluginMessage(IDENTIFIER, bytes)
                ));
    }

}
