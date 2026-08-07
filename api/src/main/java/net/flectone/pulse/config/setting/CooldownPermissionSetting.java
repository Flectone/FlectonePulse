package net.flectone.pulse.config.setting;

import net.flectone.pulse.config.Permission;

/**
 * Configuration interface for settings that include cooldown bypass permissions.
 *
 * @author TheFaser
 * @since 1.7.1
 */
public interface CooldownPermissionSetting {

    /**
     * Gets the permission entry for bypassing cooldowns.
     *
     * @return the cooldown bypass permission configuration
     * @see Permission.PermissionEntry
     */
    Permission.PermissionEntry cooldownBypass();

}