package net.flectone.pulse.platform.registry;

import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;

public interface PermissionRegistry extends Registry {

    void register(String name, Permission.Type type);

    default void register(PermissionSetting permissionSetting) {
        if (permissionSetting == null) return;

        register(permissionSetting.name(), permissionSetting.type());
    }

}
