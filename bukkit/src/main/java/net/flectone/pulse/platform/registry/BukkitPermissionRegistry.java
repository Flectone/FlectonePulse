package net.flectone.pulse.platform.registry;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BukkitPermissionRegistry implements PermissionRegistry {

    @Override
    public void register(String name, net.flectone.pulse.config.Permission.Type type) {
        if (StringUtils.isEmpty(name)) return;
        if (type == null) return;

        String stringType = type.name();

        Permission permission = Bukkit.getPluginManager().getPermission(name);
        if (permission != null) {
            if (permission.getDefault().name().equalsIgnoreCase(stringType)) return;

            // does not always work correctly, requires a full restart
            Bukkit.getPluginManager().removePermission(permission);
        }

        Bukkit.getPluginManager().addPermission(new Permission(name, PermissionDefault.getByName(stringType)));
    }

    @Override
    public void reload() {}

}
