package net.flectone.pulse.module.message.bubble;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.module.message.bubble.listener.HytaleBubbleListener;
import net.flectone.pulse.module.message.bubble.service.BubbleService;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;

@Singleton
public class HytaleBubbleModule extends BubbleModule {

    private final ListenerRegistry listenerRegistry;

    @Inject
    public HytaleBubbleModule(FileFacade fileFacade,
                              TaskScheduler taskScheduler,
                              BubbleService bubbleService,
                              ListenerRegistry listenerRegistry,
                              ModuleController moduleController,
                              FLogger fLogger) {
        super(fileFacade, taskScheduler, bubbleService, listenerRegistry, moduleController, fLogger);

        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        listenerRegistry.register(HytaleBubbleListener.class);
    }

}
