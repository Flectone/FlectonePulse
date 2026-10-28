package net.flectone.pulse.platform.adapter;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mojang.serialization.DynamicOps;
import io.netty.buffer.ByteBuf;
import lombok.SneakyThrows;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.flectone.pulse.resolver.ReflectionResolver;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.util.function.Consumer;

@Singleton
public class FabricVersionAdapterV_1_20_5 extends FabricVersionAdapterV_1_20_2 {

    private MethodHandle jsonParseStringHandle;

    private CustomPacketPayload.Type<@NonNull ProxyPayload> payloadType;
    private StreamCodec<@NonNull FriendlyByteBuf, @NonNull ProxyPayload> streamCodec;

    @Inject
    public void resolveJsonParseString(ReflectionResolver reflectionResolver) {
        Class<?> jsonParserClass = reflectionResolver.resolveClass("com.google.", "gson.JsonParser");
        this.jsonParseStringHandle = jsonParserClass == null ? null : reflectionResolver.unreflectMethod(jsonParserClass, "parseString", String.class);
    }

    @Override
    public Component literal(String message) {
        return Component.literal(message);
    }

    // we are relocating Adventure and Gson, so we can't call methods directly
    // but I think this approach is better than creating a separate module in project and using it
    @SneakyThrows
    @Override
    @SuppressWarnings("unchecked")
    public Component fromJson(String json) {
        Object jsonElement = jsonParseStringHandle.invoke(json);
        DynamicOps<Object> ops = (DynamicOps<Object>) (DynamicOps<?>) com.mojang.serialization.JsonOps.INSTANCE;

        return ComponentSerialization.CODEC.parse(ops, jsonElement).getOrThrow();
    }

    @Override
    public @Nullable MobEffectInstance getEffect(ServerPlayer player, int id) {
        MobEffect effect = BuiltInRegistries.MOB_EFFECT.byId(id);
        if (effect == null) return null;

        return player.getEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect));
    }

    @Override
    public @Nullable Component getCustomName(ItemStack itemStack) {
        return itemStack.get(DataComponents.CUSTOM_NAME);
    }

    @Override
    public String getItemPath(ItemStack itemStack) {
        return BuiltInRegistries.ITEM.getKey(itemStack.getItem()).getPath();
    }

    @Override
    public void writeItemStack(ByteBuf byteBuf, ItemStack itemStack, MinecraftServer minecraftServer) {
        ItemStack.OPTIONAL_STREAM_CODEC.encode(new RegistryFriendlyByteBuf(byteBuf, minecraftServer.registryAccess()), itemStack);
    }

    @Override
    public void registerProxyChannel(String channel, Consumer<byte[]> consumer) {
        payloadType = new CustomPacketPayload.Type<>(ResourceLocation.tryParse(channel));

        if (streamCodec == null) {
            streamCodec = StreamCodec.of(
                    (buf, payload) -> buf.writeBytes(payload.data()),
                    buf -> {
                        byte[] data = new byte[buf.readableBytes()];
                        buf.readBytes(data);
                        return new ProxyPayload(payloadType, data);
                    }
            );

            PayloadTypeRegistry.playS2C().register(payloadType, streamCodec);
            PayloadTypeRegistry.playC2S().register(payloadType, streamCodec);
        }

        ServerPlayNetworking.registerGlobalReceiver(payloadType, (payload, _) ->
                consumer.accept(payload.data())
        );
    }

    @Override
    public void unregisterProxyChannel() {
        ServerPlayNetworking.unregisterGlobalReceiver(payloadType.id());
        payloadType = null;
    }

    @Override
    public void sendProxyMessage(ServerPlayer player, byte[] data) {
        ServerPlayNetworking.send(player, new ProxyPayload(payloadType, data));
    }

    public record ProxyPayload(
            CustomPacketPayload.Type<@NonNull ProxyPayload> type,
            byte[] data
    ) implements CustomPacketPayload {
    }

}
