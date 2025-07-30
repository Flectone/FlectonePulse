package net.flectone.pulse.processor;

import lombok.experimental.UtilityClass;
import net.flectone.pulse.constant.MessageType;

import java.io.*;
import java.util.UUID;

@UtilityClass
public class ProxyMessageProcessor {

    public byte[] create(MessageType tag, UUID uuid) {
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
             DataOutputStream output = new DataOutputStream(byteStream)) {

            output.writeUTF(tag.toProxyTag());
            output.writeUTF(uuid.toString());

            return byteStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create message", e);
        }
    }

    public byte[] create(byte[] data) {
        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
             DataInputStream input = new DataInputStream(byteStream)) {

            String tag = input.readUTF();
            if (!tag.startsWith("FlectonePulse")) return null;

            MessageType proxyMessageType = MessageType.fromProxyString(tag);
            if (proxyMessageType == null) return null;

            return data;
        } catch (IOException e) {
            throw new RuntimeException("Failed to process message", e);
        }
    }

}
