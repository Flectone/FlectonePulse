package net.flectone.pulse.processing.serializer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PacketSerializer {

    public static final String MINECRAFT_BRAND = "minecraft:brand";

    public byte[] serialize(String string) {
        ByteBuf buf = Unpooled.buffer();

        writeString(buf, string);

        byte[] result = new byte[buf.readableBytes()];
        buf.readBytes(result);
        return result;
    }

    private void writeString(ByteBuf buf, String data) {
        byte[] bytes = data.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        writeVarInt(buf, bytes.length);
        buf.writeBytes(bytes);
    }

    private void writeVarInt(ByteBuf buf, int value) {
        while ((value & ~0x7F) != 0) {
            buf.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }

        buf.writeByte(value);
    }
}
