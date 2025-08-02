package net.flectone.pulse.platform.registry;

import net.flectone.pulse.config.Permission;

public interface PermissionRegistry extends Registry {

    void register(String name, Permission.Type type);

}
