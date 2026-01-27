package net.flectone.pulse.module.message.format.world;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.util.Ticker;
import net.flectone.pulse.module.message.format.world.listener.WorldHytaleListener;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
public class HytaleWorldModule extends WorldModule {

    private final ListenerRegistry listenerRegistry;
    private final TaskScheduler taskScheduler;

    @Inject
    public HytaleWorldModule(FileFacade fileFacade,
                             FPlayerService fPlayerService,
                             PlatformPlayerAdapter platformPlayerAdapter,
                             ListenerRegistry listenerRegistry,
                             TaskScheduler taskScheduler) {
        super(fileFacade, fPlayerService, platformPlayerAdapter, listenerRegistry, taskScheduler);

        this.listenerRegistry = listenerRegistry;
        this.taskScheduler = taskScheduler;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        Ticker ticker = config().ticker();
        if (ticker.enable()) {
            taskScheduler.runPlayerRegionTimer(this::update, ticker.period());
        }

        listenerRegistry.register(WorldHytaleListener.class);
    }

}
