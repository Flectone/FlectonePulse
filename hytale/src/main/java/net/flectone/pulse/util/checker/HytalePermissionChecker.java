package net.flectone.pulse.util.checker;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
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

    private final IntegrationModule integrationModule;
    private final HytalePermissionRegistry hytalePermissionRegistry;
    private final HytalePlayerAdapter hytalePlayerAdapter;

    @Override
    public boolean check(FEntity entity, String permission) {
        if (permission == null) return true;
        if (!(entity instanceof FPlayer fPlayer) || fPlayer.isUnknown()) return true;
        if (integrationModule.hasFPlayerPermission(fPlayer, permission)) return true;

        Permission.Type hytalePermission = hytalePermissionRegistry.getPermissions().get(permission);
        boolean value = hytalePermission == Permission.Type.TRUE || hytalePlayerAdapter.isOperator(fPlayer);

        PlayerRef player = hytalePlayerAdapter.getPlayer(entity.getUuid());
        if (player != null) {
            value = value || PermissionsModule.get().hasPermission(entity.getUuid(), permission); // player has no default permissions
        }

        return value;
    }

}
