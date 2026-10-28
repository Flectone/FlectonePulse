package net.flectone.pulse.platform.adapter;

import com.google.inject.Singleton;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@Singleton
public class FabricVersionAdapterV_1_16 extends FabricVersionAdapterV_1_14 {

    @Override
    public Component literal(String message) {
        return new TextComponent(message);
    }

    @Override
    public Component fromJson(String json) {
        return Component.Serializer.fromJson(json);
    }

    @Override
    public ServerLevel getLevel(ServerPlayer player) {
        return (ServerLevel) player.getCommandSenderWorld();
    }

    @Override
    public String getWorldName(ServerPlayer player) {
        return getLevel(player).dimension().location().getPath();
    }

    @Override
    public @Nullable Entity getEntity(ServerLevel serverLevel, UUID uuid) {
        return serverLevel.getEntity(uuid);
    }

    @Override
    public void sendSystemMessage(ServerPlayer player, Component component) {
        player.sendMessage(component, Util.NIL_UUID);
    }

}
