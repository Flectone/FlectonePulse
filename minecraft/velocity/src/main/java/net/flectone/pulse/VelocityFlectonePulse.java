package net.flectone.pulse;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import net.flectone.pulse.processing.processor.ProxyMessageProcessor;
import net.flectone.pulse.util.constant.LoginStatus;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.logging.FLogger;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

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

    private final Set<UUID> firstJoinPlayers = new CopyOnWriteArraySet<>();


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
    public void onPluginMessageEvent(PluginMessageEvent event) {
        if (!event.getIdentifier().equals(IDENTIFIER)) return;

        Optional<byte[]> data = ProxyMessageProcessor.validate(event.getData());
        if (data.isEmpty()) return;

        proxyServer.getAllServers().stream()
                .filter(registeredServer -> !registeredServer.getPlayersConnected().isEmpty())
                .forEach(serverInfo -> serverInfo.sendPluginMessage(IDENTIFIER, data.get()));
    }

    @Subscribe
    public void onProxyShutdownEvent(ProxyShutdownEvent event) {
        fLogger.logDisabling();

        proxyServer.getChannelRegistrar().unregister(IDENTIFIER);
        proxyServer.getEventManager().unregisterListeners(this);

        fLogger.logDisabled();
    }

    @Subscribe
    public void onServerConnectedEvent(ServerConnectedEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        if (event.getPreviousServer().isEmpty()) {
            firstJoinPlayers.add(playerUUID);

            proxyServer.getScheduler().buildTask(this, () -> {
                if (!player.isActive()) {
                    firstJoinPlayers.remove(playerUUID);
                }
            }).delay(1L, TimeUnit.SECONDS).schedule();
        }
    }

    @Subscribe
    public void onServerPostConnectEvent(ServerPostConnectEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        byte[] dataOnline = ProxyMessageProcessor.create(ModuleName.SYSTEM_ONLINE, playerUUID);

        proxyServer.getAllServers().stream()
                .filter(registeredServer -> !registeredServer.getPlayersConnected().isEmpty())
                .forEach(serverInfo -> serverInfo.sendPluginMessage(IDENTIFIER, dataOnline));

        if (firstJoinPlayers.remove(playerUUID)) {
            byte[] data = ProxyMessageProcessor.create(ModuleName.SYSTEM_CONNECTED, playerUUID);

            proxyServer.getAllServers().stream()
                    .filter(registeredServer -> !registeredServer.getPlayersConnected().isEmpty())
                    .forEach(serverInfo -> {
                        serverInfo.sendPluginMessage(IDENTIFIER, data);
                    });
        }
    }

    @Subscribe
    public void onDisconnectEvent(DisconnectEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        if (velocityLoginStateListener.getLoginStatus(playerUUID) != LoginStatus.CONNECTED) return;

        byte[] data = ProxyMessageProcessor.create(ModuleName.SYSTEM_OFFLINE, playerUUID);

        proxyServer.getAllServers().stream()
                .filter(registeredServer -> !registeredServer.getPlayersConnected().isEmpty())
                .forEach(serverInfo -> serverInfo.sendPluginMessage(IDENTIFIER, data));
    }

}
