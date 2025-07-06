package net.flectone.pulse.sender;

import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import net.flectone.pulse.listener.BukkitProxyListener;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.util.DataConsumer;
import net.flectone.pulse.util.MessageTag;
import net.flectone.pulse.util.logging.FLogger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Set;

@Singleton
public class BukkitProxySender extends ProxySender {

    private final FileResolver fileResolver;
    private final FLogger fLogger;
    private final Plugin plugin;
    private final Gson gson;
    private final Provider<BukkitProxyListener> proxyListenerProvider;
    private final MessagePipeline messagePipeline;

    @Inject
    public BukkitProxySender(FileResolver fileResolver,
                             FLogger fLogger,
                             Plugin plugin,
                             Gson gson,
                             Provider<BukkitProxyListener> proxyListenerProvider,
                             MessagePipeline messagePipeline) {
        super(fileResolver, fLogger);

        this.fileResolver = fileResolver;
        this.fLogger = fLogger;
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
    public boolean sendMessage(FEntity sender, MessageTag tag, DataConsumer<DataOutputStream> outputConsumer) {
        if (!isEnable()) return false;
        if (tag == null) return false;
        if (outputConsumer == null) return false;

        Set<String> clusters = fileResolver.getConfig().getClusters();

        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
             DataOutputStream output = new DataOutputStream(byteStream)) {

            output.writeUTF(tag.toProxyTag());

            output.writeInt(clusters.size());
            for (String cluster : clusters) {
                output.writeUTF(cluster);
            }

            String constantName = getConstantName(sender);
            sender.setConstantName(constantName);

            output.writeBoolean(sender instanceof FPlayer);
            output.writeUTF(gson.toJson(sender));
            outputConsumer.accept(output);

            Player player = Bukkit.getPlayer(sender.getUuid());
            player = player != null ? player : Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
            if (player == null) return false;

            player.sendPluginMessage(plugin, getChannel(), byteStream.toByteArray());
            return true;
        } catch (IOException e) {
            fLogger.warning(e);
        }

        return false;
    }

    private String getConstantName(FEntity sender) {
        String message = fileResolver.getLocalization(sender).getMessage().getFormat().getName_().getConstant();
        if (message.isEmpty()) return "";

        return messagePipeline.builder(sender, message).defaultSerializerBuild();
    }
}
