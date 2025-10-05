package net.flectone.pulse.platform.controller;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import net.flectone.pulse.execution.dispatcher.EventDispatcher;
import net.flectone.pulse.model.event.module.ModuleDisableEvent;
import net.flectone.pulse.model.event.module.ModuleEnableEvent;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.Module;
import net.flectone.pulse.platform.sender.CooldownSender;
import net.flectone.pulse.platform.sender.DisableSender;
import net.flectone.pulse.platform.sender.MuteSender;
import net.flectone.pulse.util.checker.PermissionChecker;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

@Singleton
public class ModuleController {

    private final Injector injector;
    private final EventDispatcher eventDispatcher;
    private final PermissionChecker permissionChecker;
    private final DisableSender disableSender;
    private final CooldownSender cooldownSender;
    private final MuteSender muteSender;

    @Inject
    public ModuleController(Injector injector,
                            EventDispatcher eventDispatcher,
                            PermissionChecker permissionChecker,
                            DisableSender disableSender,
                            CooldownSender cooldownSender,
                            MuteSender muteSender) {
        this.injector = injector;
        this.eventDispatcher = eventDispatcher;
        this.permissionChecker = permissionChecker;
        this.disableSender = disableSender;
        this.cooldownSender = cooldownSender;
        this.muteSender = muteSender;
    }

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
        enable(clazz, module -> module.config().isEnable());
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
            ModuleDisableEvent preDisableEvent = new ModuleDisableEvent(module);

            eventDispatcher.dispatch(preDisableEvent);

            if (preDisableEvent.isCancelled()) {
                // nothing
            } else {
                module.onDisable();
            }
        }

        addDefaultPredicates(module);

        module.setEnable(enablePredicate.test(module));

        if (module.isEnable()) {
            ModuleEnableEvent preEnableEvent = new ModuleEnableEvent(module);

            eventDispatcher.dispatch(preEnableEvent);

            if (preEnableEvent.isCancelled()) {
                module.setEnable(false);
            } else {
                module.onEnable();
            }
        }

        Predicate<AbstractModule> childPredicate =abstractModule -> module.isEnable() && abstractModule.config().isEnable();
        module.getChildren().forEach(subModule -> enable(subModule, childPredicate));
    }

    public void addDefaultPredicates(AbstractModule module) {
        module.getPredicates().clear();

        module.addPredicate(fPlayer -> !module.isEnable());
        module.addPredicate(fPlayer -> !permissionChecker.check(fPlayer, module.getPermission()));

        if (module instanceof AbstractModuleLocalization<?> localizationModule) {
            module.addPredicate((fPlayer, needBoolean) -> needBoolean && disableSender.sendIfDisabled(fPlayer, fPlayer, localizationModule.getMessageType()));
            module.addPredicate((fPlayer, needBoolean) -> needBoolean && cooldownSender.sendIfCooldown(fPlayer, localizationModule.getModuleCooldown()));
            module.addPredicate((fPlayer, needBoolean) -> needBoolean && muteSender.sendIfMuted(fPlayer));
        }
    }

}
