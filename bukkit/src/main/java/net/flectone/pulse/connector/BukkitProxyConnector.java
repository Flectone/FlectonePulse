package net.flectone.pulse.connector;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.util.logging.FLogger;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.platform.proxy.BukkitProxyListener;
import net.flectone.pulse.util.MessageTag;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Set;
import java.util.function.Consumer;

@Singleton
public class BukkitProxyConnector extends ProxyConnector {

    private final FileManager fileManager;
    private final Plugin plugin;
    private final Gson gson;

    @Inject private BukkitProxyListener proxyListener;

    @Inject
    public BukkitProxyConnector(FileManager fileManager,
                                FLogger fLogger,
                                Plugin plugin,
                                Gson gson) {
        super(fileManager, fLogger);

        this.fileManager = fileManager;
        this.plugin = plugin;
        this.gson = gson;
    }

    @Override
    public void reload() {
        super.reload();

        String channel = getChannel();
        if (channel == null) return;

        plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(plugin);
        plugin.getServer().getMessenger().unregisterIncomingPluginChannel(plugin);
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, channel);
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, channel, proxyListener);
    }

    @Override
    public void disable() {
        if (getChannel() == null) return;

        plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(plugin);
        plugin.getServer().getMessenger().unregisterIncomingPluginChannel(plugin);
    }

    @Override
    public boolean sendMessage(FEntity sender, MessageTag tag, Consumer<ByteArrayDataOutput> outputConsumer) {
        if (!isEnable()) return false;
        if (tag == null) return false;
        if (outputConsumer == null) return false;

        Set<String> clusters = fileManager.getConfig().getClusters();

        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF(tag.toProxyTag());

        out.writeInt(clusters.size());
        for (String cluster : clusters) {
            out.writeUTF(cluster);
        }

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
