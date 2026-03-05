package net.flectone.pulse.platform.controller;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.execution.dispatcher.EventDispatcher;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.event.module.ModuleDisableEvent;
import net.flectone.pulse.model.event.module.ModuleEnableEvent;
import net.flectone.pulse.module.*;
import net.flectone.pulse.module.Module;
import net.flectone.pulse.module.command.ban.BanModule;
import net.flectone.pulse.module.command.banlist.BanlistModule;
import net.flectone.pulse.module.command.kick.KickModule;
import net.flectone.pulse.module.command.mute.MuteModule;
import net.flectone.pulse.module.command.mutelist.MutelistModule;
import net.flectone.pulse.module.command.unban.UnbanModule;
import net.flectone.pulse.module.command.unmute.UnmuteModule;
import net.flectone.pulse.module.command.unwarn.UnwarnModule;
import net.flectone.pulse.module.command.warn.WarnModule;
import net.flectone.pulse.module.command.warnlist.WarnlistModule;
import net.flectone.pulse.platform.registry.PermissionRegistry;
import net.flectone.pulse.platform.sender.CooldownSender;
import net.flectone.pulse.platform.sender.DisableSender;
import net.flectone.pulse.platform.sender.MuteSender;
import net.flectone.pulse.util.checker.PermissionChecker;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ModuleController {

    public static final Set<Class<? extends ModuleSimple>> BAN_MODULES = Set.of(BanModule.class, BanlistModule.class, UnbanModule.class);
    public static final Set<Class<? extends ModuleSimple>> MUTE_MODULES = Set.of(MuteModule.class, MutelistModule.class, UnmuteModule.class);
    public static final Set<Class<? extends ModuleSimple>> WARN_MODULES = Set.of(WarnModule.class, WarnlistModule.class, UnwarnModule.class);
    public static final Set<Class<? extends ModuleSimple>> KICK_MODULES = Set.of(KickModule.class);

    private final Object2ObjectOpenHashMap<Class<? extends ModuleSimple>, Class<? extends ModuleSimple>> moduleRootMap = new Object2ObjectOpenHashMap<>();
    private final Object2BooleanOpenHashMap<Class<? extends ModuleSimple>> moduleStateMap = new Object2BooleanOpenHashMap<>();
    private final Object2ObjectOpenHashMap<Class<? extends ModuleSimple>, List<Class<? extends ModuleSimple>>> moduleChildrenMap = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectOpenHashMap<Class<? extends ModuleSimple>, BiPredicate<FEntity, Boolean>> modulePredicateMap = new Object2ObjectOpenHashMap<>();

    private final Injector injector;
    private final EventDispatcher eventDispatcher;
    private final Provider<PermissionChecker> permissionCheckerProvider;
    private final Provider<DisableSender> disableSenderProvider;
    private final Provider<CooldownSender> cooldownSenderProvider;
    private final Provider<MuteSender> muteSenderProvider;
    private final PermissionRegistry permissionRegistry;

    public Map<String, String> collectModuleStatuses() {
        return collectModuleStatuses(Module.class);
    }

    public Map<String, String> collectModuleStatuses(Class<? extends ModuleSimple> clazz) {
        Class<? extends ModuleSimple> root = getRoot(clazz);

        Map<String, String> modules = new Object2ObjectArrayMap<>();
        modules.put(root.getSimpleName(), isEnable(root) ? "true" : "false");

        getChildren(root)
                .forEach(subModule -> modules.putAll(collectModuleStatuses(subModule)));

        return modules;
    }

    public boolean isEnable(ModuleSimple abstractModule) {
        return isEnable(abstractModule.getClass());
    }

    public boolean isEnable(Class<? extends ModuleSimple> clazz) {
        Class<? extends ModuleSimple> root = getRoot(clazz);
        return moduleStateMap.getBoolean(root);
    }

    public boolean containsChild(ModuleSimple abstractModule, Class<? extends ModuleSimple> child) {
        return containsChild(abstractModule.getClass(), child);
    }

    public boolean containsChild(Class<? extends ModuleSimple> clazz, Class<? extends ModuleSimple> child) {
        return getChildren(clazz).contains(child);
    }

    public boolean isDisabledFor(ModuleSimple abstractModule, FEntity entity) {
        return isDisabledFor(abstractModule, entity, false);
    }

    public boolean isDisabledFor(Class<? extends ModuleSimple> clazz, FEntity entity) {
        return isDisabledFor(clazz, entity, false);
    }

    public boolean isDisabledFor(ModuleSimple abstractModule, FEntity entity, boolean isMessage) {
        return isDisabledFor(abstractModule.getClass(), entity, isMessage);
    }

    public boolean isDisabledFor(Class<? extends ModuleSimple> clazz, FEntity entity, boolean isMessage) {
        Class<? extends ModuleSimple> root = getRoot(clazz);
        BiPredicate<FEntity, Boolean> disablePredicate = modulePredicateMap.get(root);
        return disablePredicate != null && disablePredicate.test(entity, isMessage);
    }

    public List<Class<? extends ModuleSimple>> getChildren(Class<? extends ModuleSimple> clazz) {
        Class<? extends ModuleSimple> root = getRoot(clazz);
        return moduleChildrenMap.getOrDefault(root, Collections.emptyList());
    }

    public void reload() {
        reload(Module.class);
    }

    public void reload(Class<? extends ModuleSimple> clazz) {
        configureHierarchy(clazz);

        enable(clazz, module -> module.config().enable());
    }

    public void terminate() {
        terminate(Module.class);
    }

    public void terminate(Class<? extends ModuleSimple> clazz) {
        enable(clazz, module -> false);
    }

    private void configureHierarchy(Class<? extends ModuleSimple> clazz) {
        Class<? extends ModuleSimple> root = findRootSuperclass(clazz);
        moduleRootMap.put(clazz, root);

        ModuleSimple module = injector.getInstance(root);
        moduleChildrenMap.put(root, module.childrenBuilder().build());
        modulePredicateMap.put(root, buildDisablePredicate(module));

        getChildren(root).forEach(this::configureHierarchy);
    }

    public void enable(Class<? extends ModuleSimple> clazz, Predicate<ModuleSimple> enablePredicate) {
        Class<? extends ModuleSimple> root = getRoot(clazz);
        ModuleSimple module = injector.getInstance(root);

        if (isEnable(root)) {
            ModuleDisableEvent preDisableEvent = eventDispatcher.dispatch(new ModuleDisableEvent(module));
            if (!preDisableEvent.cancelled()) {
                module.onDisable();
            }
        }

        boolean newState = enablePredicate.test(module);
        moduleStateMap.put(root, newState);

        if (newState) {
            ModuleEnableEvent preEnableEvent = eventDispatcher.dispatch(new ModuleEnableEvent(module));
            if (preEnableEvent.cancelled()) {
                moduleStateMap.put(root, false);
            } else {
                module.permissionBuilder().build().forEach(permissionRegistry::register);
                module.onEnable();
            }
        }

        Predicate<ModuleSimple> childPredicate = childModule -> isEnable(root) && childModule.config().enable();
        getChildren(root).forEach(childModule -> enable(childModule, childPredicate));
    }

    public BiPredicate<FEntity, Boolean> buildDisablePredicate(ModuleSimple module) {
        BiPredicate<FEntity, Boolean> disablePredicate = module.disablePredicate()
                .or((fPlayer, needBoolean) -> !isEnable(module))
                .or((fPlayer, needBoolean) -> !permissionCheckerProvider.get().check(fPlayer, module.permission()));

        if (module instanceof ModuleLocalization<?> localizationModule) {
            return disablePredicate
                    .or((fPlayer, needBoolean) -> needBoolean && disableSenderProvider.get().sendIfDisabled(fPlayer, fPlayer, localizationModule.name()))
                    .or((fPlayer, needBoolean) -> needBoolean && cooldownSenderProvider.get().sendIfCooldown(fPlayer, localizationModule.cooldown(), module.getClass().getName()))
                    .or((fPlayer, needBoolean) -> needBoolean && muteSenderProvider.get().sendIfMuted(fPlayer));
        }

        return disablePredicate;
    }

    public boolean isInstanceOfAny(ModuleSimple module, Set<Class<? extends ModuleSimple>> classes) {
        return classes.stream().anyMatch(clazz -> clazz.isInstance(module));
    }

    public Class<? extends ModuleSimple> getRoot(Class<? extends ModuleSimple> clazz) {
        return moduleRootMap.computeIfAbsent(clazz, this::findRootSuperclass);
    }

    @SuppressWarnings("unchecked")
    private Class<? extends ModuleSimple> findRootSuperclass(Class<? extends ModuleSimple> clazz) {
        Class<?> root = clazz;
        while (root.getSuperclass() != null
                && ModuleSimple.class.isAssignableFrom(root.getSuperclass())
                && !isBaseModuleClass(root.getSuperclass())) {
            root = root.getSuperclass();
        }

        return (Class<? extends ModuleSimple>) root;
    }

    private boolean isBaseModuleClass(Class<?> clazz) {
        return clazz == ModuleSimple.class
                || clazz == ModuleLocalization.class
                || clazz == ModuleCommand.class
                || clazz == ModuleListLocalization.class;
    }
}