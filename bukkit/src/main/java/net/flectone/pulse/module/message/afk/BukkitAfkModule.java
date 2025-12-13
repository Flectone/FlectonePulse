package net.flectone.pulse.module.message.afk;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.afk.listener.AfkListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.service.FPlayerService;

@Singleton
public class BukkitAfkModule extends AfkModule {

    private final ListenerRegistry listenerRegistry;

    @Inject
    public BukkitAfkModule(FileResolver fileResolver,
                           FPlayerService fPlayerService,
                           TaskScheduler taskScheduler,
                           IntegrationModule integrationModule,
                           PlatformPlayerAdapter platformPlayerAdapter,
                           ListenerRegistry listenerRegistry) {
        super(fileResolver, fPlayerService, taskScheduler, integrationModule, platformPlayerAdapter, listenerRegistry);

        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        listenerRegistry.register(AfkListener.class);
    }
}
