package net.flectone.pulse.platform.adapter;

import com.google.inject.Singleton;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.dimension.DimensionType;

@Singleton
public class FabricVersionAdapterV_1_16 extends FabricVersionAdapterV_1_17 {

    @Override
    public String getWorldName(ServerPlayer player) {
        return DimensionType.getName(player.dimension).getPath();
    }

    @Override
    public void sendSystemMessage(ServerPlayer player, Component component) {
        player.sendMessage(component);
    }

}
