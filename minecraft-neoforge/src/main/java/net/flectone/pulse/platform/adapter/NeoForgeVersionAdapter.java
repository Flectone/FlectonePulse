package net.flectone.pulse.platform.adapter;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public interface NeoForgeVersionAdapter {

    boolean isOperator(MinecraftServer minecraftServer, UUID uuid, String name);

    boolean hasPermission(ServerPlayer player);

    <T extends CustomPacketPayload> CustomPacketPayload.Type<T> createPayloadType(String channel);

    String getPath(ResourceKey<?> resourceKey);

    UUID getId(GameProfile profile);

    String getName(GameProfile profile);

    PropertyMap getProperties(GameProfile profile);

    @Nullable Component getCustomName(ItemStack itemStack);

}
