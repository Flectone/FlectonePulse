package net.flectone.pulse;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import net.flectone.pulse.processor.ProxyMessageProcessor;
import net.flectone.pulse.util.MessageTag;
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
public class FlectonePulseVelocity implements FlectonePulse {

    private final MinecraftChannelIdentifier IDENTIFIER = MinecraftChannelIdentifier.from("flectonepulse:main");

    @Inject
    private ProxyServer proxyServer;

    @Inject
    private Logger logger;

    private FLogger fLogger;

    @Subscribe
    public void onProxyInitializeEvent(ProxyInitializeEvent event) {
        fLogger = new FLogger(logRecord -> logger.info(logRecord.getMessage()));
        fLogger.logEnabling();

        proxyServer.getChannelRegistrar().register(IDENTIFIER);

        fLogger.logPluginInfo();
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
    public void onLoginEvent(PostLoginEvent event) {
        byte[] data = ProxyMessageProcessor.create(MessageTag.SYSTEM_ONLINE, event.getPlayer().getUniqueId());

        proxyServer.getAllServers().stream()
                .filter(registeredServer -> !registeredServer.getPlayersConnected().isEmpty())
                .forEach(serverInfo -> serverInfo.sendPluginMessage(IDENTIFIER, data));
    }

    @Subscribe
    public void onDisconnectEvent(DisconnectEvent event) {
        byte[] data = ProxyMessageProcessor.create(MessageTag.SYSTEM_OFFLINE, event.getPlayer().getUniqueId());

        proxyServer.getAllServers().stream()
                .filter(registeredServer -> !registeredServer.getPlayersConnected().isEmpty())
                .forEach(serverInfo -> serverInfo.sendPluginMessage(IDENTIFIER, data));
    }

    @Override
    public void reload() {
        fLogger.logReloading();

        proxyServer.getChannelRegistrar().unregister(IDENTIFIER);
        proxyServer.getChannelRegistrar().register(IDENTIFIER);

        fLogger.logReloaded();
    }
}
