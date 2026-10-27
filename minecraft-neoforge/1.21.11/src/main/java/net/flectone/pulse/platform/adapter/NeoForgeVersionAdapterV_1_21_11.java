package net.flectone.pulse.platform.adapter;

import com.google.inject.Singleton;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;

@Singleton
public class NeoForgeVersionAdapterV_1_21_11 extends NeoForgeVersionAdapterV_1_21_9 {

    private static final Permission TRUE_PERMISSION = new Permission.HasCommandLevel(PermissionLevel.ALL);

    @Override
    public boolean hasPermission(ServerPlayer player) {
        return player.permissions().hasPermission(TRUE_PERMISSION);
    }

    @Override
    public <T extends CustomPacketPayload> CustomPacketPayload.Type<T> createPayloadType(String channel) {
        return new CustomPacketPayload.Type<>(Identifier.parse(channel));
    }

    @Override
    public String getPath(ResourceKey<?> resourceKey) {
        return resourceKey.identifier().getPath();
    }

}
