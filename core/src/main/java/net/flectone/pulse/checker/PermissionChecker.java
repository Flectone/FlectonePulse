package net.flectone.pulse.checker;

import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.model.FEntity;

public interface PermissionChecker {

    boolean check(FEntity sender, String permission);

    boolean check(FEntity sender, Permission.IPermission permission);

}
