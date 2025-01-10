package net.flectone.pulse.manager;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.logger.FLogger;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.platform.proxy.BukkitProxyListener;
import net.flectone.pulse.util.MessageTag;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

@Singleton
public class BukkitProxyManager extends ProxyManager {

    private final Plugin plugin;
    private final Gson gson;

    @Inject private BukkitProxyListener proxyListener;

    @Inject
    public BukkitProxyManager(FileManager fileManager,
                              FLogger fLogger,
                              Plugin plugin,
                              Gson gson) {
        super(fileManager, fLogger);
        this.plugin = plugin;
        this.gson = gson;
    }

    @Override
    public void reloadChannel() {
        plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(plugin);
        plugin.getServer().getMessenger().unregisterIncomingPluginChannel(plugin);
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, getChannel());
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, getChannel(), proxyListener);
    }

    @Override
    public void disable() {
        plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(plugin);
        plugin.getServer().getMessenger().unregisterIncomingPluginChannel(plugin);
    }

    @Override
    public boolean sendMessage(FEntity sender, MessageTag tag, Consumer<ByteArrayDataOutput> outputConsumer) {
        if (!isEnabledProxy()) return false;
        if (tag == null) return false;
        if (outputConsumer == null) return false;

        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF(tag.toProxyTag());
        out.writeBoolean(sender instanceof FPlayer);
        out.writeUTF(gson.toJson(sender));
        outputConsumer.accept(out);

        Player player = Bukkit.getPlayer(sender.getUuid());
        player = player != null ? player : Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
        if (player == null) return false;

        player.sendPluginMessage(plugin, getChannel(), out.toByteArray());
        return true;
    }
}
