package net.flectone.pulse.config.setting;

import net.flectone.pulse.config.Permission;

public interface CommandPermissionSetting extends PermissionSetting {

    Permission.PermissionEntry cooldownBypass();

    Permission.PermissionEntry sound();

}
