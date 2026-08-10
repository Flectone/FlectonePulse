package net.flectone.pulse.platform.proxy;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.listener.proxy.ProxyMessageProcessor;
import net.flectone.pulse.model.entity.FEntity;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BukkitProxy implements Proxy {

    private final FileFacade fileFacade;
    private final Plugin plugin;
    private final ProxyMessageProcessor proxyMessageProcessor;

    @Override
    public boolean isEnable() {
        return fileFacade.config().proxy().bungeecord() || fileFacade.config().proxy().velocity();
    }

    @Override
    public void onEnable() {
        plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(plugin);
        plugin.getServer().getMessenger().unregisterIncomingPluginChannel(plugin);
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, Proxy.CHANNEL);
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, Proxy.CHANNEL, (proxyChannel, _, message) -> {
            if (!proxyChannel.equals(Proxy.CHANNEL) || !isEnable()) {
                return;
            }

            proxyMessageProcessor.process(message);
        });
    }

    @Override
    public void onDisable() {
        if (!isEnable()) return;

        plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(plugin);
        plugin.getServer().getMessenger().unregisterIncomingPluginChannel(plugin);
    }

    @Override
    public boolean sendMessage(@NonNull FEntity sender, @NonNull ModuleName tag, byte @NonNull [] message) {
        if (!isEnable()) return false;

        Player player = getOnlinePlayer(sender);
        if (player == null || !player.isOnline()) return false;

        player.sendPluginMessage(plugin, Proxy.CHANNEL, message);
        return true;
    }

    @Nullable
    private Player getOnlinePlayer(FEntity sender) {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player.class::cast)
                .filter(player -> !player.getUniqueId().equals(sender.uuid())) // we always need another player, because sender may no longer be on the server
                .findAny()
                .orElse(Bukkit.getPlayer(sender.uuid()));
    }
}
