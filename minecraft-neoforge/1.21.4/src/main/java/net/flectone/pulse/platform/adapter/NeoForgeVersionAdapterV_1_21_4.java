package net.flectone.pulse.platform.adapter;

import com.google.inject.Singleton;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

@Singleton
public class NeoForgeVersionAdapterV_1_21_4 extends NeoForgeVersionAdapterV_1_21_1 {

    @Override
    public @Nullable Component getCustomName(ItemStack itemStack) {
        return itemStack.getCustomName();
    }

}
