package net.flectone.pulse.module.message.bubble;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.message.bubble.listener.BubblePacketListener;
import net.flectone.pulse.module.message.bubble.listener.BubblePulseListener;
import net.flectone.pulse.module.message.bubble.service.BubbleService;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.util.file.FileFacade;
import org.jspecify.annotations.NonNull;

import java.util.List;

@Singleton
public class MinecraftBubbleModule extends BubbleModule {

    private final TaskScheduler taskScheduler;
    private final BubbleService bubbleService;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public MinecraftBubbleModule(FileFacade fileFacade,
                                 BubbleService bubbleService,
                                 ListenerRegistry listenerRegistry,
                                 TaskScheduler taskScheduler) {
        super(fileFacade);

        this.taskScheduler = taskScheduler;
        this.bubbleService = bubbleService;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        bubbleService.startTicker();

        listenerRegistry.register(BubblePulseListener.class);
        listenerRegistry.register(BubblePacketListener.class);
    }

    @Override
    public void add(@NonNull FPlayer fPlayer, @NonNull String inputString, List<FPlayer> receivers) {
        taskScheduler.runRegion(fPlayer, () -> {
            if (isModuleDisabledFor(fPlayer)) return;

            bubbleService.addMessage(fPlayer, inputString, receivers);
        });
    }
    
}
