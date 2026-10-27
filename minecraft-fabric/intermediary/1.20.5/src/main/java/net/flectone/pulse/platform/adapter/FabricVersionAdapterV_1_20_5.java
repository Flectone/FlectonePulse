package net.flectone.pulse.platform.adapter;

import com.google.inject.Singleton;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

@Singleton
public class FabricVersionAdapterV_1_20_5 extends FabricVersionAdapterV_1_21_2 {

    private ResourceLocation proxyChannel;

    @Override
    public @Nullable MobEffectInstance getEffect(ServerPlayer player, int id) {
        MobEffect effect = BuiltInRegistries.MOB_EFFECT.byId(id);
        if (effect == null) return null;

        return player.getEffect(effect);
    }

    @Override
    public @Nullable Component getCustomName(ItemStack itemStack) {
        return itemStack.hasCustomHoverName() ? itemStack.getHoverName() : null;
    }

    @Override
    public void writeItemStack(ByteBuf byteBuf, ItemStack itemStack, MinecraftServer minecraftServer) {
        new FriendlyByteBuf(byteBuf).writeItem(itemStack);
    }

    @Override
    public void registerProxyChannel(String channel, Consumer<byte[]> consumer) {
        proxyChannel = ResourceLocation.tryParse(channel);

        ServerPlayNetworking.registerGlobalReceiver(proxyChannel, (server, player, handler, buf, responseSender) ->
                consumer.accept(ByteBufUtil.getBytes(buf))
        );
    }

    @Override
    public void unregisterProxyChannel() {
        ServerPlayNetworking.unregisterGlobalReceiver(proxyChannel);
        proxyChannel = null;
    }

    @Override
    public void sendProxyMessage(ServerPlayer player, byte[] data) {
        ServerPlayNetworking.send(player, proxyChannel, new FriendlyByteBuf(Unpooled.wrappedBuffer(data)));
    }

}
