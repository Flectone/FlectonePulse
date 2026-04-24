package net.flectone.pulse.module.message.rightclick;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.module.message.rightclick.listener.PacketRightclickListener;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
public class MinecraftRightClickModule extends RightclickModule {

    private final ListenerRegistry listenerRegistry;

    @Inject
    public MinecraftRightClickModule(FileFacade fileFacade,
                                     FPlayerService fPlayerService,
                                     PlatformPlayerAdapter platformPlayerAdapter,
                                     ListenerRegistry listenerRegistry,
                                     TaskScheduler taskScheduler,
                                     MessagePipeline messagePipeline,
                                     MessageDispatcher messageDispatcher,
                                     ModuleController moduleController) {
        super(fileFacade, fPlayerService, platformPlayerAdapter, taskScheduler, messagePipeline, messageDispatcher, moduleController);

        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        listenerRegistry.register(PacketRightclickListener.class);
    }
}
