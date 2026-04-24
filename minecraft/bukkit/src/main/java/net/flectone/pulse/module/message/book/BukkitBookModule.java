package net.flectone.pulse.module.message.book;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.module.message.book.listener.BukkitBookListener;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.BukkitListenerRegistry;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
public class BukkitBookModule extends BookModule {

    private final ListenerRegistry listenerRegistry;

    @Inject
    public BukkitBookModule(FileFacade fileFacade,
                            BukkitListenerRegistry listenerRegistry,
                            MessagePipeline messagePipeline,
                            ModuleController moduleController) {
        super(fileFacade, messagePipeline, moduleController);

        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        listenerRegistry.register(BukkitBookListener.class);
    }
}
