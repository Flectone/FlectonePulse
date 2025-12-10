package net.flectone.pulse.util.checker;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.FabricFlectonePulse;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.integration.FabricIntegrationModule;
import net.flectone.pulse.platform.adapter.FabricPlayerAdapter;
import net.flectone.pulse.platform.registry.FabricPermissionRegistry;
import net.minecraft.command.permission.PermissionLevel;
import net.minecraft.command.permission.PermissionPredicate;
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

    private net.minecraft.command.permission.Permission operatorPermission;

    @Override
    public boolean check(FEntity entity, String permission) {
        if (permission == null) return true;
        if (!(entity instanceof FPlayer fPlayer) || fPlayer.isUnknown()) return true;

        MinecraftServer minecraftServer = fabricFlectonePulse.getMinecraftServer();
        if (minecraftServer == null) return true;

        int operatorPermissionLevel = minecraftServer.getOpPermissionLevel().getLevel().getLevel();
        int permissionLevel = fabricPermissionRegistry.getPermissions().getOrDefault(permission, operatorPermissionLevel);

        boolean value = permissionLevel == 0;

        ServerPlayerEntity player = fabricPlayerAdapter.getPlayer(entity.getUuid());
        if (player != null) {
            PermissionPredicate permissionPredicate = player.getPermissions();

            value = switch (permissionLevel) {
                case 0 -> permissionPredicate.hasPermission(TRUE_PERMISSION); // TRUE
                case 1 -> false; // FALSE
                case 2 -> permissionPredicate.hasPermission(getOpPermission(operatorPermissionLevel)); // OP
                case 3 -> !permissionPredicate.hasPermission(getOpPermission(operatorPermissionLevel)); // NOT_OP
                default -> permissionPredicate.hasPermission(getOpPermission(operatorPermissionLevel));
            } || permissionPredicate.hasPermission(getOpPermission(operatorPermissionLevel));
        }

        return value || integrationModule.hasFPlayerPermission(fPlayer, permission);
    }

    @Override
    public boolean check(FEntity entity, Permission.IPermission permission) {
        return permission == null || check(entity, permission.getName());
    }

    private net.minecraft.command.permission.Permission getOpPermission(int operatorPermissionLevel) {
        if (operatorPermission != null) return operatorPermission;

        operatorPermission = new net.minecraft.command.permission.Permission.Level(PermissionLevel.fromLevel(operatorPermissionLevel));
        return operatorPermission;
    }

}
