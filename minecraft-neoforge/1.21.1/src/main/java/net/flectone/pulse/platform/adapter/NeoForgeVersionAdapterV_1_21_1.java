package net.flectone.pulse.platform.adapter;

import com.google.inject.Singleton;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@Singleton
public class NeoForgeVersionAdapterV_1_21_1 implements NeoForgeVersionAdapter {

    @Override
    public boolean isOperator(MinecraftServer minecraftServer, UUID uuid, String name) {
        return minecraftServer.getPlayerList().isOp(new GameProfile(uuid, name));
    }

    @Override
    public boolean hasPermission(ServerPlayer player) {
        return true;
    }

    @Override
    public <T extends CustomPacketPayload> CustomPacketPayload.Type<T> createPayloadType(String channel) {
        return new CustomPacketPayload.Type<>(ResourceLocation.parse(channel));
    }

    @Override
    public String getPath(ResourceKey<?> resourceKey) {
        return resourceKey.location().getPath();
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
    public @Nullable Component getCustomName(ItemStack itemStack) {
        return itemStack.get(DataComponents.CUSTOM_NAME);
    }

}
