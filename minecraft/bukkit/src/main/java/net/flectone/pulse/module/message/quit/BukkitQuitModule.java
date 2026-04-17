package net.flectone.pulse.module.message.quit;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.quit.listener.QuitListener;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.platform.sender.IntegrationSender;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
public class BukkitQuitModule extends MinecraftQuitModule {

    private final ListenerRegistry listenerRegistry;

    @Inject
    public BukkitQuitModule(FileFacade fileFacade,
                            IntegrationModule integrationModule,
                            TaskScheduler taskScheduler,
                            MessageDispatcher messageDispatcher,
                            ModuleController moduleController,
                            ListenerRegistry listenerRegistry,
                            PlatformServerAdapter platformServerAdapter,
                            IntegrationSender integrationSender,
                            ProxyRegistry proxyRegistry) {
        super(fileFacade, integrationModule, taskScheduler, messageDispatcher, moduleController, listenerRegistry, platformServerAdapter, integrationSender, proxyRegistry);

        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        listenerRegistry.register(QuitListener.class);
    }
}
