package net.flectone.pulse.platform.sender;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.platform.proxy.Proxy;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.ProxyDataConsumer;
import net.flectone.pulse.util.SafeDataOutputStream;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.logging.FLogger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@Singleton
public class ProxySender {

    private final ProxyRegistry proxyRegistry;
    private final FileResolver fileResolver;
    private final MessagePipeline messagePipeline;
    private final Gson gson;
    private final FLogger fLogger;

    @Inject
    public ProxySender(ProxyRegistry proxyRegistry,
                       FileResolver fileResolver,
                       MessagePipeline messagePipeline,
                       Gson gson,
                       FLogger fLogger) {
        this.proxyRegistry = proxyRegistry;
        this.fileResolver = fileResolver;
        this.messagePipeline = messagePipeline;
        this.gson = gson;
        this.fLogger = fLogger;
    }

    public boolean send(MessageType messageType, EventMetadata<?> eventMetadata) {
        ProxyDataConsumer<SafeDataOutputStream> proxyConsumer = eventMetadata.getProxy();
        if (proxyConsumer == null) return false;

        Range range = eventMetadata.getRange();
        if (!range.is(Range.Type.PROXY)) return false;

        FEntity sender = eventMetadata.getSender();
        return send(sender, messageType, proxyConsumer, eventMetadata.getUuid());
    }

    public boolean send(FEntity sender, MessageType tag) {
        return send(sender, tag, dataOutputStream -> {}, UUID.randomUUID());
    }

    public boolean send(FEntity sender, MessageType tag, ProxyDataConsumer<SafeDataOutputStream> outputConsumer, UUID metadataUUID) {
        boolean isPlayer = sender instanceof FPlayer;

        if (isPlayer) {
            FPlayer fPlayer = (FPlayer) sender;
            String constantName = getConstantName(fPlayer);
            fPlayer.setConstantName(constantName);
        }

        byte[] message;
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
             SafeDataOutputStream output = new SafeDataOutputStream(gson, byteStream)) {

            output.writeUTF(tag.toProxyTag());
            output.writeUTF(metadataUUID.toString());

            Set<String> clusters = fileResolver.getConfig().getProxy().getClusters();
            output.writeInt(clusters.size());
            for (String cluster : clusters) {
                output.writeUTF(cluster);
            }

            output.writeBoolean(isPlayer);
            output.writeUTF(gson.toJson(sender));

            outputConsumer.accept(output);

            message = byteStream.toByteArray();
        } catch (IOException e) {
            fLogger.warning(e);
            return false;
        }

        boolean sent = false;
        for (Proxy proxy : proxyRegistry.getProxies()) {
            if (proxy.sendMessage(sender, tag, message)) {
                sent = true;
            }
        }

        return sent;
    }


    private String getConstantName(FPlayer sender) {
        String message = fileResolver.getLocalization(sender).getMessage().getFormat().getName_().getConstant();
        if (message.isEmpty()) return "";

        return messagePipeline.builder(sender, message).defaultSerializerBuild();
    }
}
