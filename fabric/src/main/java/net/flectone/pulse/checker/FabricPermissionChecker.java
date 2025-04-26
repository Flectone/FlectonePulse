package net.flectone.pulse.checker;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.adapter.FabricPlayerAdapter;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.integration.FabricIntegrationModule;
import net.flectone.pulse.registry.FabricPermissionRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

@Singleton
public class FabricPermissionChecker implements PermissionChecker {

    private final MinecraftServer minecraftServer;
    private final FabricIntegrationModule integrationModule;
    private final FabricPlayerAdapter fabricPlayerAdapter;
    private final FabricPermissionRegistry fabricPermissionRegistry;

    @Inject
    public FabricPermissionChecker(MinecraftServer minecraftServer,
                                   FabricIntegrationModule integrationModule,
                                   FabricPlayerAdapter fabricPlayerAdapter,
                                   FabricPermissionRegistry fabricPermissionRegistry) {
        this.minecraftServer = minecraftServer;
        this.integrationModule = integrationModule;
        this.fabricPlayerAdapter = fabricPlayerAdapter;
        this.fabricPermissionRegistry = fabricPermissionRegistry;
    }

    @Override
    public boolean check(FEntity entity, String permission) {
        if (permission == null) return true;
        if (!(entity instanceof FPlayer fPlayer) || fPlayer.isUnknown()) return true;


        int bukkitPermission = fabricPermissionRegistry.getPermissions().get(permission);

        boolean value = bukkitPermission == 0;

        ServerPlayerEntity player = fabricPlayerAdapter.getPlayer(entity.getUuid());
        if (player != null) {
            value = player.hasPermissionLevel(bukkitPermission) || player.hasPermissionLevel(minecraftServer.getOpPermissionLevel());
        }

        return value || integrationModule.hasFPlayerPermission(fPlayer, permission);
    }

    @Override
    public boolean check(FEntity entity, net.flectone.pulse.configuration.Permission.IPermission permission) {
        return permission == null || check(entity, permission.getName());
    }

}
