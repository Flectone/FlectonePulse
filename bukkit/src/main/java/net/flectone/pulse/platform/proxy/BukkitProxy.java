package net.flectone.pulse.platform.proxy;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Config;
import net.flectone.pulse.listener.BukkitProxyListener;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

@Singleton
public class BukkitProxy implements Proxy {

    private final Config config;
    private final Plugin plugin;
    private final Provider<BukkitProxyListener> proxyListenerProvider;

    private String channel;

    @Inject
    public BukkitProxy(FileResolver fileResolver,
                       Plugin plugin,
                       Provider<BukkitProxyListener> proxyListenerProvider) {
        this.config = fileResolver.getConfig();
        this.plugin = plugin;
        this.proxyListenerProvider = proxyListenerProvider;
    }

    @Override
    public boolean isEnable() {
        return channel != null;
    }

    @Override
    public void onEnable() {
        channel = getChannel();
        if (channel == null) return;

        plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(plugin);
        plugin.getServer().getMessenger().unregisterIncomingPluginChannel(plugin);
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, channel);
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, channel, proxyListenerProvider.get());
    }

    @Override
    public void onDisable() {
        if (!isEnable()) return;

        plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(plugin);
        plugin.getServer().getMessenger().unregisterIncomingPluginChannel(plugin);

        channel = null;
    }

    @Override
    public boolean sendMessage(FEntity sender, MessageType tag, byte[] message) {
        if (!isEnable()) return false;
        if (tag == null) return false;

        Player player = Bukkit.getPlayer(sender.getUuid());
        if (player == null) {
            player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
        }

        if (player == null) return false;

        player.sendPluginMessage(plugin, channel, message);
        return true;
    }

    @Nullable
    public String getChannel() {
        if (config.getProxy().isBungeecord()) {
            return  "BungeeCord";
        }

        if (config.getProxy().isVelocity()) {
            return "flectonepulse:main";
        }

        return null;
    }
}
