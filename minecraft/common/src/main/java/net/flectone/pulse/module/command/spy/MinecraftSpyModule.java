package net.flectone.pulse.module.command.spy;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.module.command.spy.listener.MinecraftPacketSpyListener;
import net.flectone.pulse.platform.controller.ModuleCommandController;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
public class MinecraftSpyModule extends SpyModule {

    private final ListenerRegistry listenerRegistry;

    @Inject
    public MinecraftSpyModule(FileFacade fileFacade,
                              SocialService socialService,
                              PermissionChecker permissionChecker,
                              MessageDispatcher messageDispatcher,
                              ModuleController moduleController,
                              ModuleCommandController commandModuleController,
                              ProxyRegistry proxyRegistry,
                              ListenerRegistry listenerRegistry,
                              TaskScheduler taskScheduler,
                              FPlayerService fPlayerService) {
        super(fileFacade, socialService, permissionChecker, messageDispatcher, moduleController, commandModuleController, proxyRegistry, listenerRegistry, taskScheduler, fPlayerService);

        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        listenerRegistry.register(MinecraftPacketSpyListener.class);
    }

}
