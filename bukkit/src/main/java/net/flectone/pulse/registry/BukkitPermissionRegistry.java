package net.flectone.pulse.registry;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

@Singleton
public class BukkitPermissionRegistry implements PermissionRegistry {

    @Inject
    public BukkitPermissionRegistry() {
    }

    @Override
    public void register(String name, String type) {
        Permission permission = Bukkit.getPluginManager().getPermission(name);
        if (permission != null) {
            if (permission.getDefault().name().equalsIgnoreCase(type)) return;

            // does not always work correctly, requires a full restart
            Bukkit.getPluginManager().removePermission(permission);
        }

        Bukkit.getPluginManager().addPermission(new Permission(name, PermissionDefault.getByName(type)));
    }

}
