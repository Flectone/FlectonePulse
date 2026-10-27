package net.flectone.pulse.platform.adapter;

import com.google.inject.Singleton;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@Singleton
public class FabricVersionAdapterV_1_21_5 extends FabricVersionAdapterV_1_21_9 {

    @Override
    public @Nullable Entity getEntity(ServerLevel serverLevel, UUID uuid) {
        return serverLevel.getEntity(uuid);
    }

}
