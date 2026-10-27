package net.flectone.pulse.platform.adapter;

import com.google.inject.Singleton;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

@Singleton
public class FabricVersionAdapterV_1_17 extends FabricVersionAdapterV_1_19 {

    @Override
    public PlatformPlayerAdapter.Coordinates getCoordinates(ServerPlayer player) {
        BlockPos blockPos = new BlockPos(player.position());
        return new PlatformPlayerAdapter.Coordinates(blockPos.getX(), blockPos.getY(), blockPos.getZ(), player.yRot, player.xRot);
    }

    @Override
    public boolean isSneaking(ServerPlayer player) {
        return player.isShiftKeyDown();
    }

}
