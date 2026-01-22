package net.flectone.pulse.util.checker;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BukkitPermissionChecker implements PermissionChecker {

    private final IntegrationModule integrationModule;
    private final PlatformPlayerAdapter platformPlayerAdapter;

    @Override
    public boolean check(FEntity entity, String permission) {
        if (permission == null) return true;
        if (!(entity instanceof FPlayer fPlayer) || fPlayer.isUnknown()) return true;
        if (integrationModule.hasFPlayerPermission(fPlayer, permission)) return true;

        Permission bukkitPermission = Bukkit.getPluginManager().getPermission(permission);
        boolean value = (bukkitPermission != null && bukkitPermission.getDefault() == PermissionDefault.TRUE) || platformPlayerAdapter.isOperator(fPlayer);

        Player player = Bukkit.getPlayer(entity.getUuid());
        if (player != null) {
            value = value && player.hasPermission(permission);
        }

        return value;
    }

}
