package net.flectone.pulse.module.message.quit;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.quit.listener.QuitListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
public class BukkitQuitModule extends QuitModule {

    private final ListenerRegistry listenerRegistry;

    @Inject
    public BukkitQuitModule(FileFacade fileFacade,
                            IntegrationModule integrationModule,
                            ListenerRegistry listenerRegistry,
                            TaskScheduler taskScheduler) {
        super(fileFacade, integrationModule, listenerRegistry, taskScheduler);

        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        listenerRegistry.register(QuitListener.class);
    }
}
