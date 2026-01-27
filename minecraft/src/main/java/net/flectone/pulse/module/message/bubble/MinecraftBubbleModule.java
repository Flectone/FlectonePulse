package net.flectone.pulse.module.message.bubble;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.module.message.bubble.listener.BubblePacketListener;
import net.flectone.pulse.module.message.bubble.service.BubbleService;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
public class MinecraftBubbleModule extends BubbleModule {

    private final ListenerRegistry listenerRegistry;

    @Inject
    public MinecraftBubbleModule(FileFacade fileFacade,
                                 TaskScheduler taskScheduler,
                                 BubbleService bubbleService,
                                 ListenerRegistry listenerRegistry) {
        super(fileFacade, taskScheduler, bubbleService, listenerRegistry);

        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        listenerRegistry.register(BubblePacketListener.class);
    }

}
