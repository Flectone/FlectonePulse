package net.flectone.pulse.module;

import com.google.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import net.flectone.pulse.config.Config;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.platform.registry.PermissionRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

@Getter
public abstract class AbstractModule {

    private final Set<Class<? extends AbstractModule>> children = new LinkedHashSet<>();
    private final List<BiPredicate<FEntity, Boolean>> predicates = new ArrayList<>();

    @Inject
    private PermissionRegistry permissionRegistry;

    @Nullable
    private String permission;

    @Setter
    private boolean enable;

    protected AbstractModule() {
    }

    public void onEnable() {
        Permission.IPermission permission = permission();

        registerPermission(permission);

        this.permission = permission.getName();
    }

    public void onDisable() {}

    public void configureChildren() {}

    public abstract Config.IEnable config();

    public abstract Permission.IPermission permission();

    public void registerPermission(Permission.IPermission permission) {
        if (permission == null) return;

        registerPermission(permission.getName(), permission.getType());
    }

    public void registerPermission(String name, Permission.Type type) {
        permissionRegistry.register(name, type);
    }

    public void addChildren(Class<? extends AbstractModule> clazz) {
        children.add(clazz);
    }

    public void addPredicate(Predicate<FEntity> predicate) {
        predicates.add((fEntity, value) -> predicate.test(fEntity));
    }

    public void addPredicate(BiPredicate<FEntity, Boolean> biPredicate) {
        predicates.add(biPredicate);
    }

    public boolean isModuleDisabledFor(FEntity entity) {
        return isModuleDisabledFor(entity, false);
    }

    public boolean isModuleDisabledFor(FEntity entity, boolean isMessage) {
        for (BiPredicate<FEntity, Boolean> predicate : predicates) {
            if (predicate.test(entity, isMessage)) {
                return true;
            }
        }

        return false;
    }
}
