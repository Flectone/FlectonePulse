package net.flectone.pulse;

import com.google.inject.Injector;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import net.flectone.pulse.constant.HookType;
import net.flectone.pulse.constant.LoginStatus;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.listener.VelocityLoginStateListener;
import net.flectone.pulse.logging.FLogger;
import net.flectone.pulse.platform.proxy.Proxy;
import net.flectone.pulse.platform.sender.ProxySender;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

public class VelocityFlectonePulse implements LoaderBootstrap {

    private static final String UNKNOWN_SERVER_NAME = "Unknown";
    private static final MinecraftChannelIdentifier IDENTIFIER = MinecraftChannelIdentifier.from(Proxy.CHANNEL);

    private final Set<UUID> pendingConnections = Collections.synchronizedSet(new HashSet<>());

    private final Supplier<VelocityFlectonePulseLoader> loader;
    private final ProxyServer proxyServer;
    private final FLogger fLogger;
    private final VelocityLoginStateListener velocityLoginStateListener;

    public VelocityFlectonePulse(Supplier<VelocityFlectonePulseLoader> loader) {
        this.loader = loader;

        Injector injector = loader.get().getInjector();

        this.proxyServer = injector.getInstance(ProxyServer.class);

        Logger logger = injector.getInstance(Logger.class);
        this.fLogger = new FLogger(logRecord -> logger.info(logRecord.getMessage()), () -> null);

        this.velocityLoginStateListener = injector.getInstance(VelocityLoginStateListener.class);
    }

    @Override
    public void onLoad() {
        fLogger.logEnabling();

        proxyServer.getChannelRegistrar().register(IDENTIFIER);
        proxyServer.getEventManager().register(loader.get(), this);
        proxyServer.getEventManager().register(loader.get(), velocityLoginStateListener);

        fLogger.logEnabled();
    }

    @Override
    public void onDisable() {
        fLogger.logDisabling();

        proxyServer.getChannelRegistrar().unregister(IDENTIFIER);
        proxyServer.getEventManager().unregisterListeners(loader.get());

        fLogger.logDisabled();
    }

    @Override
    public void hook(HookType type, Object... args) {
        // nothing
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

        event.setResult(PluginMessageEvent.ForwardResult.handled());

        // only backend servers may talk on this channel, never clients
        if (event.getSource() instanceof ServerConnection) {
            ProxySender.send(event.getData(), bytes -> proxyServer.getAllServers().stream()
                            .filter(registeredServer -> !registeredServer.getPlayersConnected().isEmpty())
                            .forEach(registeredServer -> registeredServer.sendPluginMessage(IDENTIFIER, bytes)),
                    playerUUID -> {
                        if (pendingConnections.remove(playerUUID)) {
                            sendPlayerConnectedEvent(playerUUID, true);
                        }
                    }
            );
        }
    }

    @Subscribe
    public void onDisconnectEvent(DisconnectEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();

        // clear pending connection
        pendingConnections.remove(playerUUID);

        if (velocityLoginStateListener.getLoginStatus(playerUUID) == LoginStatus.CONNECTED) {
            String serverName = event.getPlayer().getCurrentServer()
                    .map(serverConnection -> serverConnection.getServerInfo().getName())
                    .orElse(UNKNOWN_SERVER_NAME);

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
        String serverName = proxyServer.getPlayer(playerUUID)
                .map(player -> player.getCurrentServer()
                        .map(serverConnection -> serverConnection.getServerInfo().getName())
                        .orElse(UNKNOWN_SERVER_NAME)
                )
                .orElse(UNKNOWN_SERVER_NAME);

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
