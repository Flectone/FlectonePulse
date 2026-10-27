package net.flectone.pulse.platform.adapter;

import com.google.inject.Singleton;
import net.minecraft.server.level.ServerPlayer;

@Singleton
public class FabricVersionAdapterV_1_21_11 extends FabricVersionAdapter {

    @Override
    public boolean hasPermission(ServerPlayer player) {
        return true;
    }

}
