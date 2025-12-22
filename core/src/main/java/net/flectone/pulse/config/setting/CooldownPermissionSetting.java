package net.flectone.pulse.config.setting;

import net.flectone.pulse.config.Permission;

public interface CooldownPermissionSetting extends PermissionSetting {

    Permission.PermissionEntry cooldownBypass();

}
