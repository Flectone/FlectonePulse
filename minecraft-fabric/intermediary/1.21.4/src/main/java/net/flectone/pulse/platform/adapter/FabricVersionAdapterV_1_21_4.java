package net.flectone.pulse.platform.adapter;

import com.google.inject.Singleton;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

@Singleton
public class FabricVersionAdapterV_1_21_4 extends FabricVersionAdapterV_1_21_5 {

    @Override
    public @Nullable Component getCustomName(ItemStack itemStack) {
        return itemStack.get(DataComponents.CUSTOM_NAME);
    }

}
