package net.flectone.pulse.processing.processor;

import lombok.experimental.UtilityClass;
import net.flectone.pulse.util.ProxyDataConsumer;
import net.flectone.pulse.util.constant.ModuleName;
import org.jspecify.annotations.NonNull;

import java.io.*;
import java.util.Optional;
import java.util.UUID;

@UtilityClass
public class ProxyMessageProcessor {

    public byte[] create(@NonNull ModuleName tag, @NonNull UUID uuid) {
        return create(tag, uuid, _ -> {});
    }

    public byte[] create(@NonNull ModuleName tag, @NonNull UUID uuid, @NonNull ProxyDataConsumer<DataOutputStream> outputConsumer) {
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
             DataOutputStream output = new DataOutputStream(byteStream)) {

            output.writeUTF(tag.toProxyTag());
            output.writeUTF(uuid.toString());

            outputConsumer.accept(output);

            return byteStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create message", e);
        }
    }

    public Optional<byte[]> validate(byte @NonNull [] data) {
        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
             DataInputStream input = new DataInputStream(byteStream)) {

            String tag = input.readUTF();
            if (!tag.startsWith("FlectonePulse")) return Optional.empty();

            ModuleName proxyMessageType = ModuleName.fromProxyString(tag);
            if (proxyMessageType == null) return Optional.empty();

            return Optional.of(data);
        } catch (IOException e) {
            throw new RuntimeException("Failed to process message", e);
        }
    }

}
