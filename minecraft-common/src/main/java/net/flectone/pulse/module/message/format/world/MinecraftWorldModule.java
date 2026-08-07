package net.flectone.pulse.module.message.format.world;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.model.value.Ticker;
import net.flectone.pulse.module.message.format.world.listener.MinecraftPacketWorldListener;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.provider.MinecraftPacketProvider;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.service.SocialService;

@Singleton
public class MinecraftWorldModule extends WorldModuleImpl {

    private final TaskScheduler taskScheduler;
    private final ListenerRegistry listenerRegistry;
    private final MinecraftPacketProvider packetProvider;

    @Inject
    public MinecraftWorldModule(FileFacade fileFacade,
                                SocialService socialService,
                                PlatformPlayerAdapter platformPlayerAdapter,
                                ListenerRegistry listenerRegistry,
                                TaskScheduler taskScheduler,
                                MinecraftPacketProvider packetProvider,
                                MessagePipeline messagePipeline,
                                ModuleController moduleController) {
        super(fileFacade, socialService, platformPlayerAdapter, listenerRegistry, taskScheduler, messagePipeline, moduleController);

        this.taskScheduler = taskScheduler;
        this.listenerRegistry = listenerRegistry;
        this.packetProvider = packetProvider;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        Ticker ticker = config().ticker();
        if (ticker.enable() || packetProvider.getServerVersion().isOlderThan(ServerVersion.V_1_9)) {
            taskScheduler.runPlayerAsyncTimer(this::update, ticker.period());
        }

        listenerRegistry.register(MinecraftPacketWorldListener.class);
    }

}
