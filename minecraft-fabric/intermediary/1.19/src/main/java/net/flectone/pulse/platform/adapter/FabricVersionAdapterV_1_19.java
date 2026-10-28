package net.flectone.pulse.platform.adapter;

import com.google.inject.Singleton;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

@Singleton
public class FabricVersionAdapterV_1_19 extends FabricVersionAdapterV_1_17 {

    @Override
    public Component literal(String message) {
        return Component.literal(message);
    }

    @Override
    public Component fromJson(String json) {
        return Component.Serializer.fromJson(json);
    }

    @Override
    public @Nullable ServerPlayer getPlayer(CommandSourceStack commandSourceStack) {
        return commandSourceStack.getPlayer();
    }

    @Override
    public CommandSourceStack createCommandSourceStack(ServerPlayer player) {
        return player.createCommandSourceStack();
    }

    @Override
    public void sendSystemMessage(ServerPlayer player, Component component) {
        player.sendSystemMessage(component);
    }

    @Override
    public void registerCommands(Consumer<CommandDispatcher<CommandSourceStack>> consumer) {
        CommandRegistrationCallback.EVENT.register((dispatcher, _, _) -> consumer.accept(dispatcher));
    }

}
