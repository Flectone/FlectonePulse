package net.flectone.pulse.checker;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.integration.IntegrationModule;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

@Singleton
public class BukkitPermissionChecker implements PermissionChecker {

    private final IntegrationModule integrationModule;

    @Inject
    public BukkitPermissionChecker(IntegrationModule integrationModule) {
        this.integrationModule = integrationModule;
    }

    @Override
    public boolean check(FEntity entity, String permission) {
        if (permission == null) return true;
        if (!(entity instanceof FPlayer fPlayer) || fPlayer.isUnknown()) return true;

        Permission bukkitPermission = Bukkit.getPluginManager().getPermission(permission);

        boolean value = (bukkitPermission != null && bukkitPermission.getDefault() == PermissionDefault.TRUE) ||
                Bukkit.getOperators().stream()
                        .anyMatch(offlinePlayer -> offlinePlayer.getUniqueId().equals(entity.getUuid()));

        Player player = Bukkit.getPlayer(entity.getUuid());
        if (player != null) {
            value = player.hasPermission(permission);
        }

        return value || integrationModule.hasFPlayerPermission(fPlayer, permission);
    }

    @Override
    public boolean check(FEntity entity, net.flectone.pulse.configuration.Permission.IPermission permission) {
        return permission == null || check(entity, permission.getName());
    }

}
