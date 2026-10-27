package net.flectone.pulse.platform.proxy;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.flectone.pulse.platform.adapter.FabricPayloadAdapter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public final class FabricProxyChannel {

    private static CustomPacketPayload.Type<@NonNull ProxyPayload> payloadType;
    private static StreamCodec<@NonNull FriendlyByteBuf, @NonNull ProxyPayload> streamCodec;

    private FabricProxyChannel() {
    }

    public static void register(String channel, Consumer<byte[]> consumer) {
        payloadType = new CustomPacketPayload.Type<>(Identifier.tryParse(channel));

        if (streamCodec == null) {
            streamCodec = StreamCodec.of(
                    (buf, payload) -> buf.writeBytes(payload.data()),
                    buf -> {
                        byte[] data = new byte[buf.readableBytes()];
                        buf.readBytes(data);
                        return new ProxyPayload(payloadType, data);
                    }
            );

            FabricPayloadAdapter.register(payloadType, streamCodec);
        }

        ServerPlayNetworking.registerGlobalReceiver(payloadType, (payload, _) ->
                consumer.accept(payload.data())
        );
    }

    public static void unregister() {
        ServerPlayNetworking.unregisterGlobalReceiver(payloadType.id());
        payloadType = null;
    }

    public static void send(ServerPlayer player, byte[] data) {
        ServerPlayNetworking.send(player, new ProxyPayload(payloadType, data));
    }

    public record ProxyPayload(
            CustomPacketPayload.Type<@NonNull ProxyPayload> type,
            byte[] data
    ) implements CustomPacketPayload {
    }

}
