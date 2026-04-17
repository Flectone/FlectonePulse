package net.flectone.pulse;

import net.flectone.pulse.processing.processor.ProxyMessageProcessor;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.logging.FLogger;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class FlectonePulseBungeecord extends Plugin implements Listener {

    private static final String CHANNEL = "BungeeCord";

    private FLogger fLogger;

    @Override
    public void onEnable() {
        fLogger = new FLogger(logRecord -> this.getLogger().log(logRecord), () -> null);

        fLogger.logEnabling();

        registerChannel();

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

        Optional<byte[]> data = ProxyMessageProcessor.validate(event.getData());
        if (data.isEmpty()) return;

        ProxyServer.getInstance().getServers().values().stream()
                .filter(serverInfo -> !serverInfo.getPlayers().isEmpty())
                .forEach(serverInfo -> serverInfo.sendData(CHANNEL, data.get()));
    }

    @EventHandler
    public void onServerSwitchEvent(ServerSwitchEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();

        if (event.getFrom() == null) {
            ProxyServer.getInstance().getScheduler().schedule(this, () -> {
                byte[] data = ProxyMessageProcessor.create(ModuleName.SYSTEM_CONNECTED, playerUUID);

                ProxyServer.getInstance().getServers().values().stream()
                        .filter(serverInfo -> !serverInfo.getPlayers().isEmpty())
                        .forEach(serverInfo -> serverInfo.sendData(CHANNEL, data));
            }, 500L, TimeUnit.MILLISECONDS);
        }

        byte[] data = ProxyMessageProcessor.create(ModuleName.SYSTEM_ONLINE, playerUUID);

        ProxyServer.getInstance().getServers().values().stream()
                .filter(serverInfo -> !serverInfo.getPlayers().isEmpty())
                .forEach(serverInfo -> serverInfo.sendData(CHANNEL, data));
    }

    @EventHandler
    public void onDisconnectEvent(PlayerDisconnectEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();

        ProxyServer.getInstance().getScheduler().schedule(this, () -> {
            byte[] data = ProxyMessageProcessor.create(ModuleName.SYSTEM_OFFLINE, playerUUID);

            ProxyServer.getInstance().getServers().values().stream()
                    .filter(serverInfo -> !serverInfo.getPlayers().isEmpty())
                    .forEach(serverInfo -> serverInfo.sendData(CHANNEL, data));
        }, 50L, TimeUnit.MILLISECONDS);
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
