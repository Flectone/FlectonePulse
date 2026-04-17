package net.flectone.pulse.platform.sender;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.platform.proxy.Proxy;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.util.ProxyDataConsumer;
import net.flectone.pulse.util.SafeDataOutputStream;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Sends messages and data across proxy network connections.
 *
 * <p><b>Usage example:</b>
 * <pre>{@code
 * ProxySender proxySender = flectonePulse.get(ProxySender.class);
 *
 * // Send message across proxy network
 * proxySender.send(MessageType.CHAT, eventMetadata);
 *
 * // Send custom data to proxy
 * proxySender.send(sender, MessageType.CUSTOM, output -> {
 *     output.writeUTF("custom data");
 * }, UUID.randomUUID());
 * }</pre>
 *
 * @author TheFaser
 * @since 1.0.0
 */
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ProxySender {

    private final ProxyRegistry proxyRegistry;
    private final FileFacade fileFacade;
    private final MessagePipeline messagePipeline;
    private final Gson gson;
    private final FLogger fLogger;

    /**
     * Sends event metadata to proxy network.
     *
     * @param moduleName the type of message being sent
     * @param eventMetadata the event metadata containing sender and data
     * @return true if message was sent to at least one proxy, false otherwise
     */
    public boolean send(@NonNull ModuleName moduleName, @NonNull EventMetadata<?> eventMetadata) {
        ProxyDataConsumer<SafeDataOutputStream> proxyConsumer = eventMetadata.proxy();
        if (proxyConsumer == null) return false;

        Range range = eventMetadata.range();
        if (!range.is(Range.Type.PROXY)) return false;

        FEntity sender = eventMetadata.sender();
        return send(sender, moduleName, proxyConsumer, eventMetadata.uuid(), eventMetadata);
    }

    /**
     * Sends a simple message to proxy network.
     *
     * @param sender the entity sending the message
     * @param tag the message type tag
     * @return true if message was sent to at least one proxy, false otherwise
     */
    public boolean send(@NonNull FEntity sender, @NonNull ModuleName tag) {
        return send(sender, tag, _ -> {}, UUID.randomUUID());
    }

    /**
     * Sends custom data to proxy network.
     *
     * @param sender the entity sending the data
     * @param tag the message type tag
     * @param outputConsumer consumer to write custom data to output stream
     * @param metadataUUID unique identifier for this metadata
     * @return true if data was sent to at least one proxy, false otherwise
     */
    public boolean send(@NonNull FEntity sender,
                        @NonNull ModuleName tag,
                        @NonNull ProxyDataConsumer<SafeDataOutputStream> outputConsumer,
                        @NonNull UUID metadataUUID) {
        return send(sender, tag, outputConsumer, metadataUUID, null);
    }

    /**
     * Sends custom data to proxy network.
     *
     * @param sender the entity sending the data
     * @param tag the message type tag
     * @param outputConsumer consumer to write custom data to output stream
     * @param metadataUUID unique identifier for this metadata
     * @param eventMetadata optional metadata containing additional event context
     * @return true if data was sent to at least one proxy, false otherwise
     */
    public boolean send(@NonNull FEntity sender,
                        @NonNull ModuleName tag,
                        @NonNull ProxyDataConsumer<SafeDataOutputStream> outputConsumer,
                        @NonNull UUID metadataUUID,
                        @Nullable EventMetadata<?> eventMetadata) {
        if (!proxyRegistry.hasEnabledProxy()) return false;

        if (sender instanceof FPlayer fPlayer) {
            List<String> constant = fileFacade.localization(sender).message().format().names().constant();
            if (!constant.isEmpty()) {
                sender = fPlayer.withConstants(constant.stream()
                        .map(string -> messagePipeline.build(messagePipeline.createContext(fPlayer, string)))
                        .toList()
                );
            }
        }

        byte[] message;
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
             SafeDataOutputStream output = new SafeDataOutputStream(gson, byteStream)) {

            output.writeUTF(tag.toProxyTag());
            output.writeUTF(metadataUUID.toString());
            output.writeAsJson(fileFacade.config().proxy().clusters());
            output.writeAsJson(sender);
            outputConsumer.accept(output);

            message = byteStream.toByteArray();
        } catch (IOException e) {
            fLogger.warning(e);
            return false;
        }

        boolean sent = false;
        for (Proxy proxy : proxyRegistry.getProxies()) {
            if (proxy.sendMessage(sender, tag, message, eventMetadata)) {
                sent = true;
            }
        }

        return sent;
    }

}
