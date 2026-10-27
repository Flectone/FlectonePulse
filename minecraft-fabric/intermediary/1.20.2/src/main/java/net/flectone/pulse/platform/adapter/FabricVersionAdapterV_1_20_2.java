package net.flectone.pulse.platform.adapter;

import com.github.retrooper.packetevents.PacketEvents;
import com.google.inject.Singleton;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.netty.channel.Channel;
import lombok.SneakyThrows;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;

@Singleton
public class FabricVersionAdapterV_1_20_2 extends FabricVersionAdapterV_1_20_5 {

    private Field profileField;

    @Override
    public Channel getChannel(ServerPlayer player) {
        return (Channel) PacketEvents.getAPI().getProtocolManager().getChannel(player.getUUID());
    }

    @Override
    public int getPing(ServerPlayer player) {
        return player.latency;
    }

    @Override
    public void disconnect(ServerPlayer player, String message) {
        player.connection.disconnect(literal(message));
    }

    @SneakyThrows
    @Override
    public @Nullable GameProfile getProfile(ServerLoginPacketListenerImpl listener) {
        if (profileField == null) {
            profileField = Arrays.stream(ServerLoginPacketListenerImpl.class.getDeclaredFields())
                    .filter(field -> field.getType() == GameProfile.class)
                    .findFirst()
                    .orElseThrow();

            profileField.setAccessible(true);
        }

        GameProfile profile = (GameProfile) profileField.get(listener);
        if (profile == null || profile.getId() != null) return profile;

        return new GameProfile(UUID.nameUUIDFromBytes(("OfflinePlayer:" + profile.getName()).getBytes(StandardCharsets.UTF_8)), profile.getName());
    }

    @Override
    public String getValue(Property property) {
        return property.getValue();
    }

    @Override
    public @Nullable String getLanguage(ServerPlayer player) {
        return null;
    }

    @Override
    public ServerLevel getLevel(ServerPlayer player) {
        return (ServerLevel) player.getCommandSenderWorld();
    }

}
