package net.flectone.pulse.platform.adapter;

import com.google.inject.Singleton;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

@Singleton
public class FabricVersionAdapterV_1_19_3 extends FabricVersionAdapterV_1_19_4 {

    @Override
    public @Nullable MobEffectInstance getEffect(ServerPlayer player, int id) {
        MobEffect effect = Registry.MOB_EFFECT.byId(id);
        if (effect == null) return null;

        return player.getEffect(effect);
    }

    @Override
    public String getItemPath(ItemStack itemStack) {
        return Registry.ITEM.getKey(itemStack.getItem()).getPath();
    }

}
