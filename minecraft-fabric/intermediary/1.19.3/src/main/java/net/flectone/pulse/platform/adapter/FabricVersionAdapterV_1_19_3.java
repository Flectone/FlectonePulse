package net.flectone.pulse.platform.adapter;

import com.google.inject.Singleton;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

@Singleton
public class FabricVersionAdapterV_1_19_3 extends FabricVersionAdapterV_1_19 {

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
    public String getItemPath(ItemStack itemStack) {
        return BuiltInRegistries.ITEM.getKey(itemStack.getItem()).getPath();
    }

    @Override
    public void writeItemStack(ByteBuf byteBuf, ItemStack itemStack, MinecraftServer minecraftServer) {
        new FriendlyByteBuf(byteBuf).writeItem(itemStack);
    }

}
