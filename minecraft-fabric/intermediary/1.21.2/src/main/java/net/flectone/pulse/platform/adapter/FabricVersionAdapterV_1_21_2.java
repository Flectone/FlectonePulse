package net.flectone.pulse.platform.adapter;

import com.google.inject.Singleton;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

@Singleton
public class FabricVersionAdapterV_1_21_2 extends FabricVersionAdapterV_1_21_4 {

    @Override
    public CommandSourceStack createCommandSourceStack(ServerPlayer player) {
        return player.createCommandSourceStack();
    }

    @Override
    public void sendSystemMessage(ServerPlayer player, Component component) {
        player.sendSystemMessage(component);
    }

}
