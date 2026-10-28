package net.flectone.pulse.platform.adapter;

import com.google.inject.Singleton;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.minecraft.server.players.NameAndId;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@Singleton
public class FabricVersionAdapterV_1_21_9 extends FabricVersionAdapterV_1_21_5 {

    @Override
    public @Nullable GameProfile getProfile(ServerLoginPacketListenerImpl listener) {
        return listener.authenticatedProfile;
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

    @Override
    public String getValue(Property property) {
        return property.value();
    }

    @Override
    public boolean isOperator(MinecraftServer minecraftServer, UUID uuid, String name) {
        return minecraftServer.getPlayerList().isOp(new NameAndId(uuid, name));
    }

    @Override
    public boolean hasPermission(ServerPlayer player) {
        return true;
    }

}
