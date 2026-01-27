package net.flectone.pulse.module.message.bubble;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.module.message.bubble.service.BubbleService;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
public class HytaleBubbleModule extends BubbleModule {

    @Inject
    public HytaleBubbleModule(FileFacade fileFacade,
                              TaskScheduler taskScheduler,
                              BubbleService bubbleService,
                              ListenerRegistry listenerRegistry) {
        super(fileFacade, taskScheduler, bubbleService, listenerRegistry);
    }

}
