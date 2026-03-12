package net.flectone.pulse.module.message.quit;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.quit.listener.QuitPulseListener;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
public class MinecraftQuitModule extends QuitModule {

    private final ListenerRegistry listenerRegistry;

    @Inject
    public MinecraftQuitModule(FileFacade fileFacade,
                               IntegrationModule integrationModule,
                               TaskScheduler taskScheduler,
                               MessageDispatcher messageDispatcher,
                               ModuleController moduleController,
                               ListenerRegistry listenerRegistry) {
        super(fileFacade, integrationModule, taskScheduler, messageDispatcher, moduleController);

        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        listenerRegistry.register(QuitPulseListener.class);
    }

}
