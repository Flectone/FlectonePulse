package net.flectone.pulse.module.message.format.world;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.model.value.Ticker;
import net.flectone.pulse.module.message.format.world.listener.WorldHytaleListener;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.service.SocialService;

@Singleton
public class HytaleWorldModule extends WorldModuleImpl {

    private final ListenerRegistry listenerRegistry;
    private final TaskScheduler taskScheduler;

    @Inject
    public HytaleWorldModule(FileFacade fileFacade,
                             SocialService socialService,
                             PlatformPlayerAdapter platformPlayerAdapter,
                             ListenerRegistry listenerRegistry,
                             TaskScheduler taskScheduler,
                             MessagePipeline messagePipeline,
                             ModuleController moduleController) {
        super(fileFacade, socialService, platformPlayerAdapter, listenerRegistry, taskScheduler, messagePipeline, moduleController);

        this.listenerRegistry = listenerRegistry;
        this.taskScheduler = taskScheduler;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        Ticker ticker = config().ticker();
        if (ticker.enable()) {
            taskScheduler.runPlayerAsyncTimer(this::update, ticker.period());
        }

        listenerRegistry.register(WorldHytaleListener.class);
    }

}
