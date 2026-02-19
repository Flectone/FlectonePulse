package net.flectone.pulse;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import net.flectone.pulse.processing.processor.ProxyMessageProcessor;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.logging.FLogger;
import org.slf4j.Logger;

@Plugin(
        id = "flectonepulse",
        name = "FlectonePulseVelocity",
        version = BuildConfig.PROJECT_VERSION,
        authors = BuildConfig.PROJECT_AUTHOR,
        description = BuildConfig.PROJECT_DESCRIPTION,
        url = BuildConfig.PROJECT_WEBSITE
)
public class FlectonePulseVelocity {

    private static final MinecraftChannelIdentifier IDENTIFIER = MinecraftChannelIdentifier.from("flectonepulse:main");

    private final ProxyServer proxyServer;
    private final FLogger fLogger;

    @Inject
    public FlectonePulseVelocity(ProxyServer proxyServer,
                                 Logger logger) {
        this.proxyServer = proxyServer;
        this.fLogger = new FLogger(logRecord -> logger.info(logRecord.getMessage()), () -> null);
    }

    @Subscribe
    public void onProxyInitializeEvent(ProxyInitializeEvent event) {
        fLogger.logEnabling();

        proxyServer.getChannelRegistrar().register(IDENTIFIER);

        fLogger.logEnabled();
    }

    @Subscribe
    public void onPluginMessageEvent(PluginMessageEvent event) {
        if (!event.getIdentifier().equals(IDENTIFIER)) return;

        byte[] data = ProxyMessageProcessor.create(event.getData());
        if (data == null) return;

        proxyServer.getAllServers().stream()
                .filter(registeredServer -> !registeredServer.getPlayersConnected().isEmpty())
                .forEach(serverInfo -> serverInfo.sendPluginMessage(IDENTIFIER, data));
    }

    @Subscribe
    public void onProxyShutdownEvent(ProxyShutdownEvent event) {
        fLogger.logDisabling();

        proxyServer.getChannelRegistrar().unregister(IDENTIFIER);

        fLogger.logDisabled();
    }

    @Subscribe
    public void onServerConnectedEvent(ServerConnectedEvent event) {
        byte[] data = ProxyMessageProcessor.create(MessageType.SYSTEM_ONLINE, event.getPlayer().getUniqueId());

        proxyServer.getAllServers().stream()
                .filter(registeredServer -> !registeredServer.getPlayersConnected().isEmpty())
                .forEach(serverInfo -> serverInfo.sendPluginMessage(IDENTIFIER, data));
    }

    @Subscribe
    public void onDisconnectEvent(DisconnectEvent event) {
        byte[] data = ProxyMessageProcessor.create(MessageType.SYSTEM_OFFLINE, event.getPlayer().getUniqueId());

        proxyServer.getAllServers().stream()
                .filter(registeredServer -> !registeredServer.getPlayersConnected().isEmpty())
                .forEach(serverInfo -> serverInfo.sendPluginMessage(IDENTIFIER, data));
    }

    public void reload() {
        fLogger.logReloading();

        proxyServer.getChannelRegistrar().unregister(IDENTIFIER);
        proxyServer.getChannelRegistrar().register(IDENTIFIER);

        fLogger.logReloaded();
    }
}
