package net.flectone.pulse.platform.proxy;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.processing.processor.ProxyMessageProcessor;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;
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
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, channel, (proxyChannel, _, message) -> {
            if (!proxyChannel.equals(channel) || !isEnable()) {
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

        channel = null;
    }

    @Override
    public boolean sendMessage(@NonNull FEntity sender, @NonNull ModuleName tag, byte @NonNull [] message) {
        if (!isEnable()) return false;

        Player player = getOnlinePlayer(sender);
        if (player == null || !player.isOnline()) return false;

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
    private Player getOnlinePlayer(FEntity sender) {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player.class::cast)
                .filter(player -> !player.getUniqueId().equals(sender.uuid())) // we always need another player, because sender may no longer be on the server
                .findAny()
                .orElse(Bukkit.getPlayer(sender.uuid()));
    }
}
