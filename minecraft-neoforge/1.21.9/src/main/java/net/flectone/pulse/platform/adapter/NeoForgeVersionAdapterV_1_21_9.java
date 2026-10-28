package net.flectone.pulse.platform.adapter;

import com.google.inject.Singleton;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;

import java.util.UUID;

@Singleton
public class NeoForgeVersionAdapterV_1_21_9 extends NeoForgeVersionAdapterV_1_21_4 {

    @Override
    public boolean isOperator(MinecraftServer minecraftServer, UUID uuid, String name) {
        return minecraftServer.getPlayerList().isOp(new NameAndId(uuid, name));
    }

    @Override
    public boolean hasPermission(ServerPlayer player) {
        return true;
    }

    @Override
    public UUID getId(GameProfile profile) {
        return profile.id();
    }

    @Override
    public String getName(GameProfile profile) {
        return profile.name();
    }

    @Override
    public PropertyMap getProperties(GameProfile profile) {
        return profile.properties();
    }

}
