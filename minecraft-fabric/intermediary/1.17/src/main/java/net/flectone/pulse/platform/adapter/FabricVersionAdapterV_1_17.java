package net.flectone.pulse.platform.adapter;

import com.google.inject.Singleton;
import net.minecraft.server.level.ServerPlayer;

@Singleton
public class FabricVersionAdapterV_1_17 extends FabricVersionAdapterV_1_16 {

    @Override
    public PlatformPlayerAdapter.Coordinates getCoordinates(ServerPlayer player) {
        return new PlatformPlayerAdapter.Coordinates(player.getBlockX(), player.getBlockY(), player.getBlockZ(), player.getYRot(), player.getXRot());
    }

    @Override
    public boolean isSneaking(ServerPlayer player) {
        return player.isCrouching();
    }

}
