package net.flectone.pulse.platform.controller;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.dispatcher.EventDispatcher;
import net.flectone.pulse.logging.FLogger;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.event.module.ModuleDisableEvent;
import net.flectone.pulse.model.event.module.ModuleEnableEvent;
import net.flectone.pulse.module.Module;
import net.flectone.pulse.module.ModuleLocalization;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.platform.registry.PermissionRegistry;
import net.flectone.pulse.platform.sender.CooldownSender;
import net.flectone.pulse.platform.sender.DisableSender;
import net.flectone.pulse.platform.sender.MuteSender;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.constant.ModuleName;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ModuleControllerImpl implements ModuleController {

    private final Map<ModuleName, Boolean> moduleStateMap = new EnumMap<>(ModuleName.class);
    private final Map<ModuleName, Set<ModuleName>> moduleChildrenMap = new EnumMap<>(ModuleName.class);

    private final Injector injector;
    private final EventDispatcher eventDispatcher;
    private final PermissionChecker permissionChecker;
    private final DisableSender disableSender;
    private final CooldownSender cooldownSender;
    private final MuteSender muteSender;
    private final PermissionRegistry permissionRegistry;
    private final FLogger fLogger;

    @Override
    public Map<String, String> collectModuleStatuses() {
        return collectModuleStatuses(Module.class);
    }

    @Override
    public Map<String, String> collectModuleStatuses(Class<? extends ModuleSimple> clazz) {
        ModuleSimple root = injector.getInstance(clazz);

        Map<String, String> modules = new LinkedHashMap<>();
        modules.put(root.name().name(), Boolean.toString(isEnable(root)));

        root.children().forEach(subModule -> modules.putAll(collectModuleStatuses(subModule)));

        return modules;
    }

    @Override
    public boolean isEnable(ModuleSimple module) {
        return isEnable(module.name());
    }

    @Override
    public boolean isEnable(ModuleName moduleName) {
        return Boolean.TRUE.equals(moduleStateMap.get(moduleName));
    }

    @Override
    public boolean containsChild(ModuleSimple module, ModuleName moduleName) {
        return getChildren(module).contains(moduleName);
    }

    @Override
    public boolean isDisabledFor(ModuleSimple module, FEntity fEntity) {
        return isDisabledFor(module, fEntity, false);
    }

    @Override
    public boolean isDisabledFor(ModuleSimple module, FEntity fEntity, boolean checkLocalizationModule) {
        if (!isEnable(module)) return true;
        if (!permissionChecker.check(fEntity, module.permission())) return true;

        if (checkLocalizationModule && module instanceof ModuleLocalization localizationModule) {
            if (disableSender.sendIfDisabled(fEntity, fEntity, localizationModule.name())) return true;
            if (cooldownSender.sendIfCooldown(fEntity, localizationModule.cooldown(), module.name().name())) return true;
            if (muteSender.sendIfMuted(fEntity)) return true;
        }

        return module.isDisable().test(fEntity, checkLocalizationModule);
    }


    public Set<ModuleName> getChildren(ModuleSimple moduleSimple) {
        return moduleChildrenMap.getOrDefault(moduleSimple.name(), Set.of());
    }

    private void configureHierarchy(ModuleSimple root) {
        Set<Class<? extends ModuleSimple>> children = root.children();

        moduleChildrenMap.put(root.name(), children.stream().map(clazz -> injector.getInstance(clazz).name()).collect(Collectors.toSet()));

        children.forEach(clazz -> configureHierarchy(injector.getInstance(clazz)));
    }

    @Override
    public void terminate() {
        disable(injector.getInstance(Module.class));
    }

    @Override
    public void disable(ModuleSimple module) {
        if (isEnable(module)) {
            ModuleDisableEvent preDisableEvent = eventDispatcher.dispatch(new ModuleDisableEvent(module));
            if (!preDisableEvent.cancelled()) {
                module.onDisable();
            }
        }

        moduleStateMap.put(module.name(), false);

        module.children().forEach(clazz -> disable(injector.getInstance(clazz)));
    }

    @Override
    public void initialize() {
        Module module = injector.getInstance(Module.class);

        configureHierarchy(module);
        enable(module, clazz -> clazz.config().enable());
    }

    @Override
    public void enable(ModuleSimple module, Predicate<ModuleSimple> enablePredicate) {
        boolean newState = enablePredicate.test(module);
        moduleStateMap.put(module.name(), newState);

        if (newState) {
            ModuleEnableEvent preEnableEvent = eventDispatcher.dispatch(new ModuleEnableEvent(module));
            if (preEnableEvent.cancelled()) {
                moduleStateMap.put(module.name(), false);
            } else {
                try {
                    module.permissions().forEach(permissionRegistry::register);
                    module.onEnable();
                } catch (Exception e) {
                    fLogger.warning("Failed to enable module " + module.name(), e);
                    moduleStateMap.put(module.name(), false);
                }
            }
        }

        if (isEnable(module)) {
            module.children().forEach(childModule -> enable(injector.getInstance(childModule), moduleSimple -> moduleSimple.config().enable()));
        }
    }

    @Override
    public boolean isInstanceOfAny(ModuleSimple module, Set<Class<? extends ModuleSimple>> classes) {
        return classes.stream().anyMatch(clazz -> clazz.isInstance(module));
    }

}