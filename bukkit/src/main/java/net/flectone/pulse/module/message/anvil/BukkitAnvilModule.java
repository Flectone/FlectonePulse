package net.flectone.pulse.module.message.anvil;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.module.message.anvil.listener.AnvilListener;
import net.flectone.pulse.platform.registry.BukkitListenerRegistry;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;

@Singleton
public class BukkitAnvilModule extends AnvilModule {

    private final ListenerRegistry listenerRegistry;

    @Inject
    public BukkitAnvilModule(FileResolver fileResolver,
                             BukkitListenerRegistry listenerRegistry,
                             MessagePipeline messagePipeline) {
        super(fileResolver, messagePipeline);

        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        listenerRegistry.register(AnvilListener.class);
    }
}
