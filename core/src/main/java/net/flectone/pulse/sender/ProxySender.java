package net.flectone.pulse.sender;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.constant.MessageType;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.proxy.Proxy;
import net.flectone.pulse.registry.ProxyRegistry;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.util.DataConsumer;
import net.flectone.pulse.util.logging.FLogger;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Set;

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

    public boolean send(FEntity sender, MessageType tag, DataConsumer<DataOutputStream> outputConsumer) {
        boolean isPlayer = sender instanceof FPlayer;

        if (isPlayer) {
            FPlayer fPlayer = (FPlayer) sender;
            String constantName = getConstantName(fPlayer);
            fPlayer.setConstantName(constantName);
        }

        byte[] message;
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
             DataOutputStream output = new DataOutputStream(byteStream)) {

            output.writeUTF(tag.toProxyTag());

            Set<String> clusters = fileResolver.getConfig().getClusters();
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
