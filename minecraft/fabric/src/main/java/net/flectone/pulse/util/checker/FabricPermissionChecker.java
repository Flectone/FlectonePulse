package net.flectone.pulse.util.checker;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.FabricFlectonePulse;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.integration.FabricIntegrationModule;
import net.flectone.pulse.platform.adapter.FabricPlayerAdapter;
import net.flectone.pulse.platform.registry.FabricPermissionRegistry;
import net.minecraft.command.permission.PermissionLevel;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FabricPermissionChecker implements PermissionChecker {

    private static final net.minecraft.command.permission.Permission TRUE_PERMISSION = new  net.minecraft.command.permission.Permission.Level(PermissionLevel.ALL);

    private final FabricFlectonePulse fabricFlectonePulse;
    private final FabricIntegrationModule integrationModule;
    private final FabricPlayerAdapter fabricPlayerAdapter;
    private final FabricPermissionRegistry fabricPermissionRegistry;

    @Override
    public boolean check(FEntity entity, String permission) {
        if (permission == null) return true;
        if (!(entity instanceof FPlayer fPlayer) || fPlayer.isUnknown()) return true;

        MinecraftServer minecraftServer = fabricFlectonePulse.getMinecraftServer();
        if (minecraftServer == null) return true;
        if (integrationModule.hasFPlayerPermission(fPlayer, permission)) return true;

        Integer fabricPermission = fabricPermissionRegistry.getPermissions().get(permission);
        boolean value = (fabricPermission != null && fabricPermission == 0) || fabricPlayerAdapter.isOperator(fPlayer);

        ServerPlayerEntity player = fabricPlayerAdapter.getPlayer(entity.getUuid());
        if (player != null) {
            value = value && player.getPermissions().hasPermission(TRUE_PERMISSION);
        }

        return value;
    }
}
