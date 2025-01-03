package net.flectone.pulse;

import com.google.common.io.ByteArrayDataOutput;
import net.flectone.pulse.logger.FLogger;
import net.flectone.pulse.platform.proxy.Proxy;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public final class FlectonePulseBungeecord extends Plugin implements Listener, FlectonePulse {

    private final String CHANNEL = "BungeeCord";

    private FLogger fLogger;

    @Override
    public void onEnable() {
        fLogger = new FLogger(this.getLogger(), null, null, null);

        fLogger.logEnabling();

        registerChannel();

        fLogger.logPluginInfo();
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

        ByteArrayDataOutput output = Proxy.create(event.getData());
        if (output == null) return;

        ProxyServer.getInstance().getServers()
                .values()
                .forEach(serverInfo -> serverInfo.sendData(CHANNEL, output.toByteArray()));
    }

    @Override
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
