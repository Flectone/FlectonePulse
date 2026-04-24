package net.flectone.pulse.module.message.join;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.join.listener.BukkitJoinListener;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.platform.sender.IntegrationSender;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
public class BukkitJoinModule extends MinecraftJoinModule {

    private final ListenerRegistry listenerRegistry;

    @Inject
    public BukkitJoinModule(FileFacade fileFacade,
                            PlatformPlayerAdapter platformPlayerAdapter,
                            IntegrationModule integrationModule,
                            ListenerRegistry listenerRegistry,
                            TaskScheduler taskScheduler,
                            MessageDispatcher messageDispatcher,
                            ModuleController moduleController,
                            FPlayerService fPlayerService,
                            IntegrationSender integrationSender,
                            ProxyRegistry proxyRegistry) {
        super(fileFacade, platformPlayerAdapter, integrationModule, listenerRegistry, taskScheduler, messageDispatcher, moduleController, fPlayerService, integrationSender, proxyRegistry);

        this.listenerRegistry = listenerRegistry;
    }


    @Override
    public void onEnable() {
        super.onEnable();

        listenerRegistry.register(BukkitJoinListener.class);
    }
}
