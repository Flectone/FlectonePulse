package net.flectone.pulse.platform.adapter;

import com.google.inject.Singleton;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import io.netty.channel.Channel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@Singleton
public class FabricVersionAdapterV_1_20_2 extends FabricVersionAdapterV_1_19_4 {

    @Override
    public Channel getChannel(ServerPlayer player) {
        return player.connection.connection.channel;
    }

    @Override
    public int getPing(ServerPlayer player) {
        return player.connection.latency();
    }

    @Override
    public void disconnect(ServerPlayer player, String message) {
        player.connection.disconnect(literal(message));
    }

    @Override
    public @Nullable GameProfile getProfile(ServerLoginPacketListenerImpl listener) {
        return listener.authenticatedProfile;
    }

    @Override
    public boolean isAcceptingMessages(ServerLoginPacketListenerImpl listener) {
        return listener.isAcceptingMessages();
    }

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
    public String getValue(Property property) {
        return property.value();
    }

    @Override
    public @Nullable String getLanguage(ServerPlayer player) {
        return player.clientInformation().language();
    }

    @Override
    public ServerLevel getLevel(ServerPlayer player) {
        return player.serverLevel();
    }

    @Override
    public String getWorldName(ServerPlayer player) {
        return getLevel(player).dimension().location().getPath();
    }

    @Override
    public @Nullable Entity getEntity(ServerLevel serverLevel, UUID uuid) {
        return serverLevel.getEntity(uuid);
    }

}
