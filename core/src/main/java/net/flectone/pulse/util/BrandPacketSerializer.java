package net.flectone.pulse.util;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@Singleton
public class BrandPacketSerializer {

    public static final String MINECRAFT_BRAND = "minecraft:brand";

    private final Class<?> packetDataSerializer;

    @Inject
    public BrandPacketSerializer() throws ClassNotFoundException {
        this.packetDataSerializer = Class.forName("net.minecraft.network.PacketDataSerializer");
    }

    public byte[] serialize(String string) {
        ByteBuf pds = (ByteBuf) getPacketDataSerializer();
        if (pds == null) return null;

        if (!writeString(pds, string)) return null;

        byte[] result = new byte[pds.readableBytes()];
        for (int i = 0; i < result.length; i++) {
            result[i] = pds.getByte(i);
        }

        return result;
    }


    private Object getPacketDataSerializer() {
        try {
            Constructor<?> packetDataSerializerConstructor = packetDataSerializer.getConstructor(ByteBuf.class);
            return packetDataSerializerConstructor.newInstance(Unpooled.buffer());
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            return null;
        }
    }

    private boolean writeString(Object buf, String data) {
        try {
            packetDataSerializer.getDeclaredMethod("a", String.class).invoke(buf, data);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            return false;
        }

        return true;
    }
}
