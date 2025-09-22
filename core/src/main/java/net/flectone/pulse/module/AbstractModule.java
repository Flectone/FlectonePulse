package net.flectone.pulse.module;

import com.google.inject.Inject;
import com.google.inject.Injector;
import lombok.Getter;
import lombok.Setter;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.platform.registry.PermissionRegistry;
import net.flectone.pulse.util.checker.PermissionChecker;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

@Getter
public abstract class AbstractModule {

    private final Set<Class<? extends AbstractModule>> children = new LinkedHashSet<>();
    private final List<BiPredicate<FEntity, Boolean>> predicates = new ArrayList<>();

    @Inject private PermissionRegistry permissionRegistry;
    @Inject private PermissionChecker permissionChecker;
    @Inject private Injector injector;

    private String modulePermission;

    @Setter private boolean enable;

    protected AbstractModule() {
        addDefaultPredicates();
    }

    public void onEnable() {}

    public void onDisable() {}

    protected abstract boolean isConfigEnable();

    public void registerModulePermission(Permission.IPermission permission) {
        registerPermission(permission);
        this.modulePermission = permission.getName();
    }

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

    public boolean isModuleDisabledFor(FEntity entity, boolean needBoolean) {
        for (BiPredicate<FEntity, Boolean> predicate : predicates) {
            if (predicate.test(entity, needBoolean)) {
                return true;
            }
        }

        return false;
    }

    public Map<String, String> collectModuleStatuses() {
        return collectModuleStatuses(this.getClass());
    }

    public void reload() {
        load(this.getClass());
        enable(this.getClass(), AbstractModule::isConfigEnable);
    }

    public void terminate() {
        enable(this.getClass(), module -> false);
    }

    private Map<String, String> collectModuleStatuses(Class<? extends AbstractModule> clazz) {
        AbstractModule module = injector.getInstance(clazz);

        Map<String, String> modules = new HashMap<>();

        modules.put(clazz.getSimpleName(), module.isEnable() ? "true" : "false");

        injector.getInstance(clazz)
                .getChildren()
                .forEach(subModule -> modules.putAll(collectModuleStatuses(subModule)));

        return modules;
    }

    private void load(Class<? extends AbstractModule> clazz) {
        AbstractModule module = injector.getInstance(clazz);

        module.getPredicates().clear();
        module.addDefaultPredicates();

        module.getChildren().forEach(this::load);
    }

    private void enable(Class<? extends AbstractModule> clazz, Predicate<AbstractModule> enablePredicate) {
        AbstractModule module = injector.getInstance(clazz);

        if (module.isEnable()) {
            module.onDisable();
        }

        boolean isEnabled = enablePredicate.test(module);
        module.setEnable(isEnabled);

        // if FPlayer.UNKNOWN (all-permissions) fails check (isModuleDisabledFor() will return true),
        // then another plugin/module disables this module
        if (isEnabled && !module.isModuleDisabledFor(FPlayer.UNKNOWN)) {
            module.onEnable();
        }

        Predicate<AbstractModule> childPredicate = isEnabled
                ? AbstractModule::isConfigEnable
                : abstractModule -> false;
        module.getChildren().forEach(subModule -> enable(subModule, childPredicate));
    }

    protected void addDefaultPredicates() {
        addPredicate(fPlayer -> !isEnable());
        addPredicate(fPlayer -> !permissionChecker.check(fPlayer, modulePermission));
    }
}
