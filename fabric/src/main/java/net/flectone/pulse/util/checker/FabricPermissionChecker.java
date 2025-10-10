package net.flectone.pulse.util.checker;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.FabricFlectonePulse;
import net.flectone.pulse.platform.adapter.FabricPlayerAdapter;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.integration.FabricIntegrationModule;
import net.flectone.pulse.platform.registry.FabricPermissionRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FabricPermissionChecker implements PermissionChecker {

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

        int fabricPermission = fabricPermissionRegistry.getPermissions().getOrDefault(permission, minecraftServer.getOpPermissionLevel());

        boolean value = fabricPermission == 0;

        ServerPlayerEntity player = fabricPlayerAdapter.getPlayer(entity.getUuid());
        if (player != null) {
            value = player.hasPermissionLevel(fabricPermission) || player.hasPermissionLevel(minecraftServer.getOpPermissionLevel());
        }

        return value || integrationModule.hasFPlayerPermission(fPlayer, permission);
    }

    @Override
    public boolean check(FEntity entity, Permission.IPermission permission) {
        return permission == null || check(entity, permission.getName());
    }

}
