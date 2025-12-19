package net.flectone.pulse.config.setting;

import net.flectone.pulse.config.Permission;

public interface PermissionSetting {

    String name();

    Permission.Type type();

}
