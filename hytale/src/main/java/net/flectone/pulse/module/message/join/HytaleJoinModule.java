package net.flectone.pulse.module.message.join;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.join.listener.HytalePulseJoinListener;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.service.PlaytimeService;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
public class HytaleJoinModule extends JoinModule {

    private final ListenerRegistry listenerRegistry;

    @Inject
    public HytaleJoinModule(FileFacade fileFacade,
                            PlatformPlayerAdapter platformPlayerAdapter,
                            PlatformServerAdapter platformServerAdapter,
                            IntegrationModule integrationModule,
                            TaskScheduler taskScheduler,
                            MessageDispatcher messageDispatcher,
                            ListenerRegistry listenerRegistry,
                            ModuleController moduleController,
                            PlaytimeService playtimeService,
                            ProxyRegistry proxyRegistry) {
        super(fileFacade, platformPlayerAdapter, platformServerAdapter, integrationModule, taskScheduler, messageDispatcher, moduleController, playtimeService, proxyRegistry, listenerRegistry);

        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        listenerRegistry.register(HytalePulseJoinListener.class);
    }
}
