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
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.AbstractModuleLocalization;
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

    public static final Set<Class<? extends AbstractModule>> BAN_MODULES = Set.of(BanModule.class, BanlistModule.class, UnbanModule.class);
    public static final Set<Class<? extends AbstractModule>> MUTE_MODULES = Set.of(MuteModule.class, MutelistModule.class, UnmuteModule.class);
    public static final Set<Class<? extends AbstractModule>> WARN_MODULES = Set.of(WarnModule.class, WarnlistModule.class, UnwarnModule.class);
    public static final Set<Class<? extends AbstractModule>> KICK_MODULES = Set.of(KickModule.class);

    private final Object2BooleanOpenHashMap<Class<? extends AbstractModule>> moduleStateMap = new Object2BooleanOpenHashMap<>();
    private final Object2ObjectOpenHashMap<Class<? extends AbstractModule>, List<Class<? extends AbstractModule>>> moduleChildrenMap = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectOpenHashMap<Class<? extends AbstractModule>, BiPredicate<FEntity, Boolean>> modulePredicateMap = new Object2ObjectOpenHashMap<>();

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

    public Map<String, String> collectModuleStatuses(Class<? extends AbstractModule> clazz) {
        Map<String, String> modules = new Object2ObjectArrayMap<>();

        modules.put(clazz.getSimpleName(), isEnable(clazz) ? "true" : "false");

        getChildren(clazz)
                .forEach(subModule -> modules.putAll(collectModuleStatuses(subModule)));

        return modules;
    }

    public boolean isEnable(AbstractModule abstractModule) {
        return isEnable(abstractModule.getClass());
    }

    public boolean isEnable(Class<? extends AbstractModule> clazz) {
        return moduleStateMap.getBoolean(clazz);
    }

    public boolean containsChild(AbstractModule abstractModule, Class<? extends AbstractModule> child) {
        return containsChild(abstractModule.getClass(), child);
    }

    public boolean containsChild(Class<? extends AbstractModule> clazz, Class<? extends AbstractModule> child) {
        return getChildren(clazz).contains(child);
    }

    public boolean isDisabledFor(AbstractModule abstractModule, FEntity entity) {
        return isDisabledFor(abstractModule, entity, false);
    }

    public boolean isDisabledFor(Class<? extends AbstractModule> clazz, FEntity entity) {
        return isDisabledFor(clazz, entity, false);
    }

    public boolean isDisabledFor(AbstractModule abstractModule, FEntity entity, boolean isMessage) {
        return isDisabledFor(abstractModule.getClass(), entity, isMessage);
    }

    public boolean isDisabledFor(Class<? extends AbstractModule> clazz, FEntity entity, boolean isMessage) {
        BiPredicate<FEntity, Boolean> disablePredicate = modulePredicateMap.get(clazz);
        return disablePredicate != null && disablePredicate.test(entity, isMessage);
    }

    public List<Class<? extends AbstractModule>> getChildren(Class<? extends AbstractModule> clazz) {
        return moduleChildrenMap.getOrDefault(clazz, Collections.emptyList());
    }

    public void reload() {
        reload(Module.class);
    }

    public void reload(Class<? extends AbstractModule> clazz) {
        // configure all modules
        configureChildren(clazz);

        // enable
        enable(clazz, module -> module.config().enable());
    }

    public void terminate() {
        terminate(Module.class);
    }

    public void terminate(Class<? extends AbstractModule> clazz) {
        enable(clazz, module -> false);
    }

    public void configureChildren(Class<? extends AbstractModule> clazz) {
        AbstractModule module = injector.getInstance(clazz);

        moduleChildrenMap.put(clazz, module.childrenBuilder().build());
        modulePredicateMap.put(clazz, buildDisablePredicate(module));

        getChildren(clazz).forEach(this::configureChildren);
    }

    public void enable(Class<? extends AbstractModule> clazz, Predicate<AbstractModule> enablePredicate) {
        AbstractModule module = injector.getInstance(clazz);

        if (isEnable(clazz)) {
            ModuleDisableEvent preDisableEvent = eventDispatcher.dispatch(new ModuleDisableEvent(module));

            if (preDisableEvent.cancelled()) {
                // nothing
            } else {
                module.onDisable();
            }
        }

        moduleStateMap.put(clazz, enablePredicate.test(module));

        if (isEnable(clazz)) {
            ModuleEnableEvent preEnableEvent = eventDispatcher.dispatch(new ModuleEnableEvent(module));

            if (preEnableEvent.cancelled()) {
                moduleStateMap.put(clazz, false);
            } else {
                module.permissionBuilder().build().forEach(permissionRegistry::register);
                module.onEnable();
            }
        }

        Predicate<AbstractModule> childPredicate =abstractModule -> isEnable(clazz) && abstractModule.config().enable();
        getChildren(clazz).forEach(subModule -> enable(subModule, childPredicate));
    }

    public BiPredicate<FEntity, Boolean> buildDisablePredicate(AbstractModule module) {
        BiPredicate<FEntity, Boolean> disablePredicate = module.disablePredicate()
                .and((fPlayer, needBoolean) -> !isEnable(module))
                .and((fPlayer, needBoolean) -> !permissionCheckerProvider.get().check(fPlayer, module.permission()));

        if (module instanceof AbstractModuleLocalization<?> localizationModule) {
            return disablePredicate
                    .and((fPlayer, needBoolean) -> needBoolean && disableSenderProvider.get().sendIfDisabled(fPlayer, fPlayer, localizationModule.messageType()))
                    .and((fPlayer, needBoolean) -> needBoolean && cooldownSenderProvider.get().sendIfCooldown(fPlayer, localizationModule.cooldown(), module.getClass().getName()))
                    .and((fPlayer, needBoolean) -> needBoolean && muteSenderProvider.get().sendIfMuted(fPlayer));
        }

        return disablePredicate;
    }

    public boolean isInstanceOfAny(AbstractModule module, Set<Class<? extends AbstractModule>> classes) {
        return classes.stream().anyMatch(clazz -> clazz.isInstance(module));
    }

}
