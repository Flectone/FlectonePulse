package net.flectone.pulse.platform.sender;

import net.flectone.pulse.exception.ProxyMessageCreateException;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.util.ProxyDataConsumer;
import net.flectone.pulse.util.constant.ModuleName;
import org.jspecify.annotations.NonNull;

import java.io.*;
import java.util.UUID;
import java.util.function.Consumer;

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
public interface ProxySender {

    /**
     * Reads a message off the proxy channel and either forwards it on or handles it as a join confirmation.
     *
     * @param data the raw payload
     * @param dataConsumer forwards the payload to the servers that should see it
     * @param backendJoinConfirm receives the player id when the message is a join confirmation
     */
    static void send(byte @NonNull [] data, @NonNull Consumer<byte[]> dataConsumer, @NonNull Consumer<UUID> backendJoinConfirm) {
        try (DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(data))) {

            String tag = dataInputStream.readUTF();
            if (!tag.startsWith("FlectonePulse")) return;

            ModuleName proxyMessageType = ModuleName.fromProxyString(tag);
            if (proxyMessageType == null) return;
            if (proxyMessageType == ModuleName.PLAYER_CONNECTED) {
                UUID playerUUID = UUID.fromString(dataInputStream.readUTF());
                backendJoinConfirm.accept(playerUUID);
                return;
            }

            dataConsumer.accept(data);
        } catch (IOException e) {
            throw new ProxyMessageCreateException(e);
        }
    }

    /**
     * Encodes a message for the proxy channel and hands it to the transport.
     *
     * @param tag the module the message belongs to
     * @param outputConsumer writes the payload
     * @param dataConsumer receives the encoded bytes
     */
    static void send(@NonNull ModuleName tag, @NonNull ProxyDataConsumer<DataOutputStream> outputConsumer, @NonNull Consumer<byte[]> dataConsumer) {
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
             DataOutputStream output = new DataOutputStream(byteStream)) {

            output.writeUTF(tag.toProxyTag());

            outputConsumer.accept(output);

            dataConsumer.accept(byteStream.toByteArray());
        } catch (IOException e) {
            throw new ProxyMessageCreateException(e);
        }
    }

    /**
     * Sends event metadata to proxy network.
     *
     * @param moduleName the type of message being sent
     * @param eventMetadata the event metadata containing sender and data
     * @param messageContext The message context containing sender details and unique message identifier
     * @return true if message was sent to at least one proxy, false otherwise
     */
    boolean send(@NonNull ModuleName moduleName, @NonNull EventMetadata eventMetadata, @NonNull MessageContext messageContext);

    /**
     * Sends a simple message to proxy network.
     *
     * @param sender the entity sending the message
     * @param tag the message type tag
     * @return true if message was sent to at least one proxy, false otherwise
     */
    boolean send(@NonNull FEntity sender, @NonNull ModuleName tag);

    /**
     * Sends a simple message to proxy network.
     *
     * @param sender the entity sending the message
     * @param tag the message type tag
     * @param outputConsumer consumer to write custom data to output stream
     * @return true if message was sent to at least one proxy, false otherwise
     */
    boolean send(@NonNull FEntity sender, @NonNull ModuleName tag, @NonNull ProxyDataConsumer<DataOutputStream> outputConsumer);

    /**
     * Sends custom data to proxy network.
     *
     * @param sender the entity sending the data
     * @param tag the message type tag
     * @param outputConsumer consumer to write custom data to output stream
     * @param metadataUUID unique identifier for this metadata
     * @return true if data was sent to at least one proxy, false otherwise
     */
    boolean send(@NonNull FEntity sender, @NonNull ModuleName tag, @NonNull ProxyDataConsumer<DataOutputStream> outputConsumer, @NonNull UUID metadataUUID);

}
