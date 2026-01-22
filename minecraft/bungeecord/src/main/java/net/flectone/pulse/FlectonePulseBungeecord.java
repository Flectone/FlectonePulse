package net.flectone.pulse;

import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.logging.FLogger;
import net.flectone.pulse.processing.processor.ProxyMessageProcessor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public final class FlectonePulseBungeecord extends Plugin implements Listener {

    private static final String CHANNEL = "BungeeCord";

    private FLogger fLogger;

    @Override
    public void onEnable() {
        fLogger = new FLogger(this.getLogger());

        fLogger.logEnabling();

        registerChannel();

        fLogger.logDescription();
        fLogger.logEnabled();
    }

    @Override
    public void onDisable() {
        fLogger.logDisabling();

        unregisterChannel();

        fLogger.logDisabled();
    }

    @EventHandler
    public void onPluginMessageEvent(PluginMessageEvent event) {
        if (!event.getTag().equals(CHANNEL)) return;

        byte[] data = ProxyMessageProcessor.create(event.getData());
        if (data == null) return;

        ProxyServer.getInstance().getServers().values().stream()
                .filter(serverInfo -> !serverInfo.getPlayers().isEmpty())
                .forEach(serverInfo -> serverInfo.sendData(CHANNEL, data));
    }

    @EventHandler
    public void onServerConnectedEvent(ServerConnectedEvent event) {
        byte[] data = ProxyMessageProcessor.create(MessageType.SYSTEM_ONLINE, event.getPlayer().getUniqueId());

        ProxyServer.getInstance().getServers().values().stream()
                .filter(serverInfo -> !serverInfo.getPlayers().isEmpty())
                .forEach(serverInfo -> serverInfo.sendData(CHANNEL, data));
    }

    @EventHandler
    public void onDisconnectEvent(PlayerDisconnectEvent event) {
        byte[] data = ProxyMessageProcessor.create(MessageType.SYSTEM_OFFLINE, event.getPlayer().getUniqueId());

        ProxyServer.getInstance().getServers().values().stream()
                .filter(serverInfo -> !serverInfo.getPlayers().isEmpty())
                .forEach(serverInfo -> serverInfo.sendData(CHANNEL, data));
    }

    public void reload() {
        fLogger.logReloading();

        unregisterChannel();
        registerChannel();

        fLogger.logReloaded();
    }

    private void registerChannel() {
        getProxy().registerChannel(CHANNEL);
        getProxy().getPluginManager().registerListener(this, this);
    }

    private void unregisterChannel() {
        getProxy().unregisterChannel(CHANNEL);
        getProxy().getPluginManager().unregisterListener(this);
    }
}
