package net.flectone.pulse.platform.adapter;

import com.google.inject.Singleton;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.players.NameAndId;

import java.util.UUID;

@Singleton
public class FabricVersionAdapterV_1_21_11 extends FabricVersionAdapterV_1_21_9 {

    private static final Permission TRUE_PERMISSION = new Permission.HasCommandLevel(PermissionLevel.ALL);

    @Override
    public boolean isOperator(MinecraftServer minecraftServer, UUID uuid, String name) {
        return minecraftServer.getPlayerList().isOp(new NameAndId(uuid, name));
    }

    @Override
    public boolean hasPermission(ServerPlayer player) {
        return player.permissions().hasPermission(TRUE_PERMISSION);
    }

}
