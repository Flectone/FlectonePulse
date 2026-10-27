package net.flectone.pulse.platform.adapter;

import com.google.inject.Singleton;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.server.MinecraftServer;

import java.util.UUID;

@Singleton
public class FabricVersionAdapterV_1_21_9 extends FabricVersionAdapterV_1_21_11 {

    @Override
    public UUID getId(GameProfile profile) {
        return profile.getId();
    }

    @Override
    public String getName(GameProfile profile) {
        return profile.getName();
    }

    @Override
    public PropertyMap getProperties(GameProfile profile) {
        return profile.getProperties();
    }

    @Override
    public boolean isOperator(MinecraftServer minecraftServer, UUID uuid, String name) {
        return minecraftServer.getPlayerList().isOp(new GameProfile(uuid, name));
    }

}
