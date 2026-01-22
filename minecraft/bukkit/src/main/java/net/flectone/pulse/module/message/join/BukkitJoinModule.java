package net.flectone.pulse.module.message.join;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.join.listener.JoinListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
public class BukkitJoinModule extends MinecraftJoinModule {

    private final ListenerRegistry listenerRegistry;

    @Inject
    public BukkitJoinModule(FileFacade fileFacade,
                            PlatformPlayerAdapter platformPlayerAdapter,
                            IntegrationModule integrationModule,
                            ListenerRegistry listenerRegistry,
                            TaskScheduler taskScheduler) {
        super(fileFacade, platformPlayerAdapter, integrationModule, listenerRegistry, taskScheduler);

        this.listenerRegistry = listenerRegistry;
    }


    @Override
    public void onEnable() {
        super.onEnable();

        listenerRegistry.register(JoinListener.class);
    }
}
