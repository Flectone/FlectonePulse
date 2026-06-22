package net.flectone.pulse.util.checker;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.NeoForgeFlectonePulse;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.platform.adapter.NeoForgePlayerAdapter;
import net.flectone.pulse.platform.registry.NeoForgePermissionRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class NeoForgePermissionChecker implements PermissionChecker {

    private static final net.minecraft.server.permissions.Permission TRUE_PERMISSION = new net.minecraft.server.permissions.Permission.HasCommandLevel(net.minecraft.server.permissions.PermissionLevel.ALL);

    private final NeoForgeFlectonePulse neoForgeFlectonePulse;
    private final NeoForgePlayerAdapter neoForgePlayerAdapter;
    private final NeoForgePermissionRegistry neoForgePermissionRegistry;

    @Inject
    private Provider<IntegrationModule> integrationModuleProvider;

    @Override
    public boolean check(FEntity entity, String permission) {
        if (permission == null) return true;
        if (!(entity instanceof FPlayer fPlayer) || fPlayer.isConsole()) return true;

        MinecraftServer minecraftServer = neoForgeFlectonePulse.getMinecraftServer();
        if (minecraftServer == null) return true;

        IntegrationModule integrationModule = integrationModuleProvider.get();
        if (integrationModule.hasFPlayerPermission(fPlayer, permission)) return true;

        Permission.Type neoForgePermission = neoForgePermissionRegistry.getPermissions().get(permission);

        boolean value;
        if (neoForgePermission != null) {
            if (neoForgePermission == Permission.Type.TRUE && integrationModule.isAlwaysHaveTruePermission()) return true;

            value = neoForgePermission != Permission.Type.FALSE &&
                    (neoForgePermission == Permission.Type.TRUE || neoForgePlayerAdapter.isOperator(fPlayer) && neoForgePermission != Permission.Type.NOT_OP);
        } else {
            value = neoForgePlayerAdapter.isOperator(fPlayer);
        }

        ServerPlayer player = neoForgePlayerAdapter.getPlayer(entity.uuid());
        if (player != null) {
            value = value && player.permissions().hasPermission(TRUE_PERMISSION);
        }

        return value;
    }
}
