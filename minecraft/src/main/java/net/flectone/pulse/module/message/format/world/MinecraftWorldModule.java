package net.flectone.pulse.module.message.format.world;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.util.Ticker;
import net.flectone.pulse.module.message.format.world.listener.WorldPacketListener;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
public class MinecraftWorldModule extends WorldModule {

    private final TaskScheduler taskScheduler;
    private final ListenerRegistry listenerRegistry;
    private final PacketProvider packetProvider;

    @Inject
    public MinecraftWorldModule(FileFacade fileFacade,
                                FPlayerService fPlayerService,
                                PlatformPlayerAdapter platformPlayerAdapter,
                                ListenerRegistry listenerRegistry,
                                TaskScheduler taskScheduler,
                                PacketProvider packetProvider) {
        super(fileFacade, fPlayerService, platformPlayerAdapter, listenerRegistry, taskScheduler);

        this.taskScheduler = taskScheduler;
        this.listenerRegistry = listenerRegistry;
        this.packetProvider = packetProvider;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        Ticker ticker = config().ticker();
        if (ticker.enable() || packetProvider.getServerVersion().isOlderThan(ServerVersion.V_1_9)) {
            taskScheduler.runPlayerRegionTimer(this::update, ticker.period());
        }

        listenerRegistry.register(WorldPacketListener.class);
    }

}
