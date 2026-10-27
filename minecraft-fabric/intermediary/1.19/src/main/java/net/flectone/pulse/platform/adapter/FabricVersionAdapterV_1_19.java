package net.flectone.pulse.platform.adapter;

import com.google.inject.Singleton;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

@Singleton
public class FabricVersionAdapterV_1_19 extends FabricVersionAdapterV_1_19_3 {

    @Override
    public Component literal(String message) {
        return new TextComponent(message);
    }

    @Override
    public @Nullable ServerPlayer getPlayer(CommandSourceStack commandSourceStack) {
        return commandSourceStack.getEntity() instanceof ServerPlayer player ? player : null;
    }

    @Override
    public void sendSystemMessage(ServerPlayer player, Component component) {
        player.sendMessage(component, Util.NIL_UUID);
    }

    @Override
    public void registerCommands(Consumer<CommandDispatcher<CommandSourceStack>> consumer) {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> consumer.accept(dispatcher));
    }

}
