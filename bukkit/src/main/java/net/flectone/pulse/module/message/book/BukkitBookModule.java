package net.flectone.pulse.module.message.book;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.module.message.book.listener.BookListener;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.registry.BukkitListenerRegistry;
import net.flectone.pulse.registry.ListenerRegistry;
import net.flectone.pulse.resolver.FileResolver;

@Singleton
public class BukkitBookModule extends BookModule {

    private final ListenerRegistry listenerRegistry;

    @Inject
    public BukkitBookModule(FileResolver fileResolver,
                            BukkitListenerRegistry listenerRegistry,
                            MessagePipeline messagePipeline) {
        super(fileResolver, messagePipeline);

        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        listenerRegistry.register(BookListener.class);
    }
}
