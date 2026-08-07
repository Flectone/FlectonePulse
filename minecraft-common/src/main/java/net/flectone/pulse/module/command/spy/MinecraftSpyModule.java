package net.flectone.pulse.module.command.spy;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.dispatcher.MessageDispatcher;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.module.command.spy.listener.MinecraftPacketSpyListener;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.platform.controller.ModuleCommandController;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.SocialService;

@Singleton
public class MinecraftSpyModule extends SpyModuleImpl {

    private final ListenerRegistry listenerRegistry;

    @Inject
    public MinecraftSpyModule(FileFacade fileFacade,
                              SocialService socialService,
                              PermissionChecker permissionChecker,
                              MessageDispatcher messageDispatcher,
                              MessagePipeline messagePipeline,
                              ModuleController moduleController,
                              ModuleCommandController commandModuleController,
                              ProxyRegistry proxyRegistry,
                              ListenerRegistry listenerRegistry,
                              TaskScheduler taskScheduler,
                              FPlayerService fPlayerService) {
        super(fileFacade, socialService, permissionChecker, messageDispatcher, messagePipeline, moduleController, commandModuleController, proxyRegistry, listenerRegistry, taskScheduler, fPlayerService);

        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        listenerRegistry.register(MinecraftPacketSpyListener.class);
    }

}
