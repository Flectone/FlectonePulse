package net.flectone.pulse.platform.registry;

import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;

/**
 * Registers the plugin's permissions with the platform, so permission plugins can offer them
 * for completion and apply their defaults.
 * @author TheFaser
 */
public interface PermissionRegistry extends Registry {

    /**
     * Registers one permission.
     *
     * @param name the permission node
     * @param type who holds it by default
     */
    void register(String name, Permission.Type type);

    /**
     * Registers a permission from its config entry, ignoring a missing one.
     *
     * @param permissionSetting the config entry, may be null
     */
    default void register(PermissionSetting permissionSetting) {
        if (permissionSetting == null) return;

        register(permissionSetting.name(), permissionSetting.type());
    }

}
