package net.flectone.pulse.module;

import com.google.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.util.PermissionUtil;
import net.flectone.pulse.model.FEntity;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public abstract class AbstractModule {

    @Getter
    private final Set<Class<? extends AbstractModule>> children = new HashSet<>();

    private final Set<Predicate<FEntity>> predicates = new HashSet<>();

    @Inject
    private PermissionUtil permissionUtil;

    @Getter
    private String modulePermission;

    @Getter
    @Setter
    private boolean enable;

    public AbstractModule() {
        addPredicate(fPlayer -> !isEnable());
        addPredicate(fPlayer -> !permissionUtil.has(fPlayer, modulePermission));
    }

    public abstract void reload();
    public abstract boolean isConfigEnable();

    public void registerModulePermission(Permission.IPermission permission) {
        registerPermission(permission);
        this.modulePermission = permission.getName();
    }

    public void registerPermission(Permission.IPermission permission) {
        if (permission == null) return;

        registerPermission(permission.getName(), permission.getType());
    }

    public void registerPermission(String name, Permission.Type type) {
        permissionUtil.register(name, type.name());
    }

    public void addChildren(Class<? extends AbstractModule> clazz) {
        children.add(clazz);
    }

    public void addPredicate(Predicate<FEntity> predicate) {
        predicates.add(predicate);
    }

    public boolean checkModulePredicates(FEntity entity) {
        for (Predicate<FEntity> predicate : predicates) {
            if (predicate.test(entity)) {
                return true;
            }
        }

        return false;
    }
}
