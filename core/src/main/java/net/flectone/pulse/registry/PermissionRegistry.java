package net.flectone.pulse.registry;

import net.flectone.pulse.configuration.Permission;

public interface PermissionRegistry {

    void register(String name, Permission.Type type);

    void reload();

}
