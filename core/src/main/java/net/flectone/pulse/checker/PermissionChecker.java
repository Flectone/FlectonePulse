package net.flectone.pulse.checker;

import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.model.FEntity;

public interface PermissionChecker {

    /**
     * Checks if the entity has the specified permission string.
     *
     * @param entity The entity to check permissions for
     * @param permission The permission string to verify
     * @return true if the entity has the permission, false otherwise
     */
    boolean check(FEntity entity, String permission);

    /**
     * Checks if the entity has the specified permission.
     *
     * @param entity The entity to check permissions for
     * @param permission The permission object to verify
     * @return true if the entity has the permission, false otherwise
     */
    boolean check(FEntity entity, Permission.IPermission permission);

}
