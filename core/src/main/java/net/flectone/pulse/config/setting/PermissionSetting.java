package net.flectone.pulse.config.setting;

import net.flectone.pulse.config.Permission;

/**
 * Configuration interface for permission settings.
 *
 * @author TheFaser
 * @since 1.7.1
 */
public interface PermissionSetting {

    /**
     * Gets the name of the permission.
     *
     * @return the permission name
     */
    String name();

    /**
     * Gets the type of the permission.
     *
     * @return the permission type
     * @see Permission.Type
     */
    Permission.Type type();

}