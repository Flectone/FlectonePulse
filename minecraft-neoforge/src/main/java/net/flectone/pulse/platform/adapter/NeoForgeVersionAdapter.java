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
public class NeoForgeVersionAdapter {

    public boolean isOperator(MinecraftServer minecraftServer, UUID uuid, String name) {
        return minecraftServer.getPlayerList().isOp(new GameProfile(uuid, name));
    }

    public boolean hasPermission(ServerPlayer player) {
        return true;
    }

    public <T extends CustomPacketPayload> CustomPacketPayload.Type<T> createPayloadType(String channel) {
        return new CustomPacketPayload.Type<>(ResourceLocation.parse(channel));
    }

    public String getPath(ResourceKey<?> resourceKey) {
        return resourceKey.location().getPath();
    }

    public UUID getId(GameProfile profile) {
        return profile.getId();
    }

    public String getName(GameProfile profile) {
        return profile.getName();
    }

    public PropertyMap getProperties(GameProfile profile) {
        return profile.getProperties();
    }

    public @Nullable Component getCustomName(ItemStack itemStack) {
        return itemStack.get(DataComponents.CUSTOM_NAME);
    }

}
