package net.flectone.pulse.platform.proxy;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.listener.BukkitProxyListener;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BukkitProxy implements Proxy {

    private final FileFacade fileFacade;
    private final Plugin plugin;
    private final Provider<BukkitProxyListener> proxyListenerProvider;

    private String channel;

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
    public boolean sendMessage(FEntity sender, ModuleName tag, byte[] message) {
        if (!isEnable()) return false;
        if (tag == null) return false;

        Player player = getOnlinePlayer(sender, tag);
        if (player == null) return false;

        player.sendPluginMessage(plugin, channel, message);
        return true;
    }

    public @Nullable String getChannel() {
        if (fileFacade.config().proxy().bungeecord()) {
            return "BungeeCord";
        }

        if (fileFacade.config().proxy().velocity()) {
            return "flectonepulse:main";
        }

        return null;
    }

    @Nullable
    private Player getOnlinePlayer(FEntity sender, ModuleName tag) {
        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();

        if (tag == ModuleName.MESSAGE_QUIT) {
            return onlinePlayers.stream()
                    .filter(player -> !player.getUniqueId().equals(sender.uuid()))
                    .findFirst()
                    .orElse(null);
        }

        Player player = Bukkit.getPlayer(sender.uuid());
        return player != null ? player : Iterables.getFirst(onlinePlayers, null);
    }
}
