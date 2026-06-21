package net.flectone.pulse.module.command.spy;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.module.command.spy.listener.BukkitSpyListener;
import net.flectone.pulse.platform.controller.ModuleCommandController;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.BukkitListenerRegistry;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
public class BukkitSpyModule extends MinecraftSpyModule {

    private final BukkitListenerRegistry bukkitListenerRegistry;

    @Inject
    public BukkitSpyModule(FileFacade fileFacade,
                           SocialService socialService,
                           FPlayerService fPlayerService,
                           PermissionChecker permissionChecker,
                           BukkitListenerRegistry bukkitListenerRegistry,
                           TaskScheduler taskScheduler,
                           MessageDispatcher messageDispatcher,
                           ModuleController moduleController,
                           ModuleCommandController commandModuleController,
                           ProxyRegistry proxyRegistry,
                           ListenerRegistry listenerRegistry) {
        super(fileFacade, socialService, permissionChecker, messageDispatcher, moduleController, commandModuleController, proxyRegistry, listenerRegistry, taskScheduler, fPlayerService);

        this.bukkitListenerRegistry = bukkitListenerRegistry;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        bukkitListenerRegistry.register(BukkitSpyListener.class, Event.Priority.NORMAL);
    }
}
