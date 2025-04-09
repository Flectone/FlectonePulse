package net.flectone.pulse.sender;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.util.logging.FLogger;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.listener.BukkitProxyListener;
import net.flectone.pulse.util.MessageTag;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Set;
import java.util.function.Consumer;

@Singleton
public class BukkitProxySender extends ProxySender {

    private final FileManager fileManager;
    private final Plugin plugin;
    private final Gson gson;
    private final Provider<BukkitProxyListener> proxyListenerProvider;
    private final MessagePipeline messagePipeline;

    @Inject
    public BukkitProxySender(FileManager fileManager,
                             FLogger fLogger,
                             Plugin plugin,
                             Gson gson,
                             Provider<BukkitProxyListener> proxyListenerProvider,
                             MessagePipeline messagePipeline) {
        super(fileManager, fLogger);

        this.fileManager = fileManager;
        this.plugin = plugin;
        this.gson = gson;
        this.proxyListenerProvider = proxyListenerProvider;
        this.messagePipeline = messagePipeline;
    }

    @Override
    public void reload() {
        super.reload();

        String channel = getChannel();
        if (channel == null) return;

        plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(plugin);
        plugin.getServer().getMessenger().unregisterIncomingPluginChannel(plugin);
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, channel);
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, channel, proxyListenerProvider.get());
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

        String constantName = getConstantName(sender);
        sender.setConstantName(constantName);

        out.writeBoolean(sender instanceof FPlayer);
        out.writeUTF(gson.toJson(sender));
        outputConsumer.accept(out);

        Player player = Bukkit.getPlayer(sender.getUuid());
        player = player != null ? player : Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
        if (player == null) return false;

        player.sendPluginMessage(plugin, getChannel(), out.toByteArray());
        return true;
    }

    private String getConstantName(FEntity sender) {
        String message = fileManager.getLocalization(sender).getMessage().getFormat().getName_().getConstant();
        if (message.isEmpty()) return "";

        return messagePipeline.builder(sender, message).defaultSerializerBuild();
    }
}
