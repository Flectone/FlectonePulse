package net.flectone.pulse.module.message.book;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.registry.BukkitListenerRegistry;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.module.message.book.listener.BookListener;
import net.flectone.pulse.pipeline.MessagePipeline;
import org.bukkit.event.EventPriority;

@Singleton
public class BukkitBookModule extends BookModule {

    private final BukkitListenerRegistry bukkitListenerManager;

    @Inject
    public BukkitBookModule(FileResolver fileResolver,
                            BukkitListenerRegistry bukkitListenerManager,
                            MessagePipeline messagePipeline) {
        super(fileResolver, messagePipeline);

        this.bukkitListenerManager = bukkitListenerManager;
    }

    @Override
    public void reload() {
        super.reload();

        bukkitListenerManager.register(BookListener.class, EventPriority.NORMAL);
    }
}
