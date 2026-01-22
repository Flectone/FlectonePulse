package net.flectone.pulse.module.message.anvil;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.module.message.anvil.listener.AnvilListener;
import net.flectone.pulse.platform.registry.BukkitListenerRegistry;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
public class BukkitAnvilModule extends AnvilModule {

    private final ListenerRegistry listenerRegistry;

    @Inject
    public BukkitAnvilModule(FileFacade fileFacade,
                             BukkitListenerRegistry listenerRegistry,
                             MessagePipeline messagePipeline) {
        super(fileFacade, messagePipeline);

        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        listenerRegistry.register(AnvilListener.class);
    }
}
