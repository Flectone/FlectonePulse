package net.flectone.pulse;

import net.flectone.pulse.processing.processor.ProxyMessageProcessor;
import net.flectone.pulse.util.constant.LoginStatus;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.logging.FLogger;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

public final class BungeecordFlectonePulse extends Plugin implements Listener {

    private static final String CHANNEL = "BungeeCord";

    private final Set<UUID> firstJoinPlayers = new CopyOnWriteArraySet<>();
    private final BungeecordLoginStateListener bungeeDisconnectListener = new BungeecordLoginStateListener();

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
        ProxiedPlayer player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        if (event.getFrom() == null) {
            firstJoinPlayers.add(playerUUID);

            ProxyServer.getInstance().getScheduler().schedule(this, () -> {
                if (!player.isConnected()) {
                    firstJoinPlayers.remove(playerUUID);
                }
            }, 1L, TimeUnit.SECONDS);
        }
    }

    @EventHandler
    public void onServerConnectedEvent(ServerConnectedEvent event) {
        ProxiedPlayer proxiedPlayer = event.getPlayer();
        UUID playerUUID = proxiedPlayer.getUniqueId();

        byte[] dataOnline = ProxyMessageProcessor.create(ModuleName.SYSTEM_ONLINE, playerUUID);

        ProxyServer.getInstance().getServers().values().stream()
                .filter(serverInfo -> !serverInfo.getPlayers().isEmpty())
                .forEach(serverInfo -> serverInfo.sendData(CHANNEL, dataOnline));

        if (firstJoinPlayers.remove(playerUUID)) {
            byte[] dataConnected = ProxyMessageProcessor.create(ModuleName.SYSTEM_CONNECTED, playerUUID);

            ProxyServer.getInstance().getServers().values().stream()
                    .filter(serverInfo -> !serverInfo.getPlayers().isEmpty())
                    .forEach(serverInfo -> serverInfo.sendData(CHANNEL, dataConnected));
        }
    }

    @EventHandler
    public void onDisconnectEvent(PlayerDisconnectEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        if (bungeeDisconnectListener.getLoginStatus(playerUUID) != LoginStatus.CONNECTED) return;

        byte[] data = ProxyMessageProcessor.create(ModuleName.SYSTEM_OFFLINE, playerUUID);

        ProxyServer.getInstance().getServers().values().stream()
                .filter(serverInfo -> !serverInfo.getPlayers().isEmpty())
                .forEach(serverInfo -> serverInfo.sendData(CHANNEL, data));
    }

    private void registerChannel() {
        getProxy().registerChannel(CHANNEL);
        getProxy().getPluginManager().registerListener(this, this);
        getProxy().getPluginManager().registerListener(this, bungeeDisconnectListener);
    }

    private void unregisterChannel() {
        getProxy().unregisterChannel(CHANNEL);
        getProxy().getPluginManager().unregisterListener(this);
        getProxy().getPluginManager().unregisterListener(bungeeDisconnectListener);
    }
}
