package net.flectone.pulse.util;

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
public class BukkitPermissionUtil extends PermissionUtil {

    @Inject private IntegrationModule integrationModule;

    @Inject
    public BukkitPermissionUtil() {}

    @Override
    public void register(String name, String type) {
        if (Bukkit.getPluginManager().getPermission(name) != null) return;

        Bukkit.getPluginManager().addPermission(new Permission(name, PermissionDefault.getByName(type)));
    }

    @Override
    public boolean has(FEntity sender, String permission) {
        if (permission == null) return true;
        if (!(sender instanceof FPlayer fPlayer) || fPlayer.isUnknown()) return true;

        Permission bukkitPermission = Bukkit.getPluginManager().getPermission(permission);

        boolean value = (bukkitPermission != null && bukkitPermission.getDefault() == PermissionDefault.TRUE) ||
                Bukkit.getOperators().stream()
                        .anyMatch(offlinePlayer -> offlinePlayer.getUniqueId().equals(sender.getUuid()));

        Player player = Bukkit.getPlayer(sender.getUuid());
        if (player != null) {
            value = player.hasPermission(permission);
        }

        return value || integrationModule.hasFPlayerPermission(fPlayer, permission);
    }
}
