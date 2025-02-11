package net.flectone.pulse.util;

import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.FEntity;

public abstract class PermissionUtil {

    public abstract void register(String name, String type);

    public abstract boolean has(FEntity sender, String permission);

    public boolean has(FEntity sender, Permission.IPermission permission) {
        return permission == null || has(sender, permission.getName());
    }
}
