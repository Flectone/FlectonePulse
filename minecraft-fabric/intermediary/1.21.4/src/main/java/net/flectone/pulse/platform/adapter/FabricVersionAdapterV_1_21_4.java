package net.flectone.pulse.platform.adapter;

import com.google.inject.Singleton;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

@Singleton
public class FabricVersionAdapterV_1_21_4 extends FabricVersionAdapterV_1_21_2 {

    @Override
    public @Nullable Component getCustomName(ItemStack itemStack) {
        return itemStack.getCustomName();
    }

    @Override
    public String getItemPath(ItemStack itemStack) {
        return BuiltInRegistries.ITEM.getKey(itemStack.getItem()).getPath();
    }

    @Override
    public void writeItemStack(ByteBuf byteBuf, ItemStack itemStack, MinecraftServer minecraftServer) {
        ItemStack.OPTIONAL_STREAM_CODEC.encode(new RegistryFriendlyByteBuf(byteBuf, minecraftServer.registryAccess()), itemStack);
    }

}
