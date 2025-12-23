package net.flectone.pulse.platform.controller;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.execution.dispatcher.EventDispatcher;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ModuleController {

    public static final Set<Class<? extends AbstractModule>> BAN_MODULES = Set.of(BanModule.class, BanlistModule.class, UnbanModule.class);
    public static final Set<Class<? extends AbstractModule>> MUTE_MODULES = Set.of(MuteModule.class, MutelistModule.class, UnmuteModule.class);
    public static final Set<Class<? extends AbstractModule>> WARN_MODULES = Set.of(WarnModule.class, WarnlistModule.class, UnwarnModule.class);
    public static final Set<Class<? extends AbstractModule>> KICK_MODULES = Set.of(KickModule.class);

    private final Injector injector;
    private final EventDispatcher eventDispatcher;
    private final PermissionChecker permissionChecker;
    private final DisableSender disableSender;
    private final CooldownSender cooldownSender;
    private final MuteSender muteSender;
    private final PermissionRegistry permissionRegistry;

    public Map<String, String> collectModuleStatuses() {
        return collectModuleStatuses(Module.class);
    }

    public Map<String, String> collectModuleStatuses(Class<? extends AbstractModule> clazz) {
        AbstractModule module = injector.getInstance(clazz);

        Map<String, String> modules = new HashMap<>();

        modules.put(clazz.getSimpleName(), module.isEnable() ? "true" : "false");

        injector.getInstance(clazz)
                .getChildren()
                .forEach(subModule -> modules.putAll(collectModuleStatuses(subModule)));

        return modules;
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

        module.getChildren().clear();
        module.configureChildren();

        module.getChildren().forEach(this::configureChildren);
    }

    public void enable(Class<? extends AbstractModule> clazz, Predicate<AbstractModule> enablePredicate) {
        AbstractModule module = injector.getInstance(clazz);

        if (module.isEnable()) {
            ModuleDisableEvent preDisableEvent = eventDispatcher.dispatch(new ModuleDisableEvent(module));

            if (preDisableEvent.cancelled()) {
                // nothing
            } else {
                module.onDisable();
            }
        }

        addDefaultPredicates(module);

        module.setEnable(enablePredicate.test(module));

        if (module.isEnable()) {
            ModuleEnableEvent preEnableEvent = eventDispatcher.dispatch(new ModuleEnableEvent(module));

            if (preEnableEvent.cancelled()) {
                module.setEnable(false);
            } else {
                module.permissionBuilder().build().forEach(permissionRegistry::register);
                module.onEnable();
            }
        }

        Predicate<AbstractModule> childPredicate =abstractModule -> module.isEnable() && abstractModule.config().enable();
        module.getChildren().forEach(subModule -> enable(subModule, childPredicate));
    }

    public void addDefaultPredicates(AbstractModule module) {
        module.getPredicates().clear();

        module.addPredicate(fPlayer -> !module.isEnable());
        module.addPredicate(fPlayer -> !permissionChecker.check(fPlayer, module.permission()));

        if (module instanceof AbstractModuleLocalization<?> localizationModule) {
            module.addPredicate((fPlayer, needBoolean) -> needBoolean && disableSender.sendIfDisabled(fPlayer, fPlayer, localizationModule.messageType()));
            module.addPredicate((fPlayer, needBoolean) -> needBoolean && cooldownSender.sendIfCooldown(fPlayer, localizationModule.cooldown()));
            module.addPredicate((fPlayer, needBoolean) -> needBoolean && muteSender.sendIfMuted(fPlayer));
        }
    }

    public boolean isInstanceOfAny(AbstractModule module, Set<Class<? extends AbstractModule>> classes) {
        return classes.stream().anyMatch(clazz -> clazz.isInstance(module));
    }

}
