package net.flectone.pulse.util.checker;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.platform.adapter.HytalePlayerAdapter;
import net.flectone.pulse.platform.registry.HytalePermissionRegistry;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class HytalePermissionChecker implements PermissionChecker {

    private final HytalePermissionRegistry hytalePermissionRegistry;
    private final HytalePlayerAdapter hytalePlayerAdapter;

    @Inject
    private Provider<IntegrationModule> integrationModuleProvider;

    @Override
    public boolean check(FEntity entity, String permission) {
        if (permission == null) return true;
        if (!(entity instanceof FPlayer fPlayer) || fPlayer.isConsole()) return true;

        IntegrationModule integrationModule = integrationModuleProvider.get();
        if (integrationModule.hasFPlayerPermission(fPlayer, permission)) return true;

        Permission.Type hytalePermission = hytalePermissionRegistry.getPermissions().get(permission);

        boolean value;
        if (hytalePermission != null) {
            if (hytalePermission == Permission.Type.TRUE && integrationModule.isAlwaysHaveTruePermission()) return true;

            value = hytalePermission != Permission.Type.FALSE &&
                    (hytalePermission == Permission.Type.TRUE || hytalePlayerAdapter.isOperator(fPlayer) && hytalePermission != Permission.Type.NOT_OP);
        } else {
            value = hytalePlayerAdapter.isOperator(fPlayer);
        }

        return PermissionsModule.get().hasPermission(entity.uuid(), permission, value);
    }

}
