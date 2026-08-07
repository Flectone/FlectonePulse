package net.flectone.pulse.config.setting;

import net.flectone.pulse.config.Permission;

/**
 * Configuration interface for settings that include sound permissions.
 *
 * @author TheFaser
 * @since 1.7.1
 */
public interface SoundPermissionSetting {

    /**
     * Gets the permission entry for playing sounds.
     *
     * @return the sound permission configuration
     * @see Permission.PermissionEntry
     */
    Permission.PermissionEntry sound();

}