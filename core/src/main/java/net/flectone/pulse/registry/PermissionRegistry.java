package net.flectone.pulse.registry;

import net.flectone.pulse.configuration.Permission;

public interface PermissionRegistry extends Registry {

    void register(String name, Permission.Type type);

}
