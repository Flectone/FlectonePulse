package net.flectone.pulse.util.checker;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.util.LazyInstance;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BukkitPermissionChecker implements PermissionChecker {

    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final LazyInstance<IntegrationModule> integrationModule;

    @Override
    public boolean check(FEntity entity, String permission) {
        if (permission == null) return true;
        if (!(entity instanceof FPlayer fPlayer) || fPlayer.isConsole()) return true;

        IntegrationModule integrationModuleInstance = integrationModule.get();
        if (integrationModuleInstance.hasFPlayerPermission(fPlayer, permission)) return true;

        Permission bukkitPermission = Bukkit.getPluginManager().getPermission(permission);

        boolean value;
        if (bukkitPermission != null) {
            PermissionDefault permissionDefault = bukkitPermission.getDefault();
            if (permissionDefault == PermissionDefault.TRUE && integrationModuleInstance.isAlwaysHaveTruePermission()) return true;

            value = permissionDefault != PermissionDefault.FALSE &&
                    (permissionDefault == PermissionDefault.TRUE || platformPlayerAdapter.isOperator(fPlayer) && permissionDefault != PermissionDefault.NOT_OP);
        } else {
            value = platformPlayerAdapter.isOperator(fPlayer);
        }

        Player player = Bukkit.getPlayer(entity.uuid());
        if (player != null) {
            value = value && player.hasPermission(permission);
        }

        return value;
    }

}