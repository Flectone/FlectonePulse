package net.flectone.pulse.module.message.quit;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.quit.listener.QuitListener;
import net.flectone.pulse.registry.BukkitListenerRegistry;
import net.flectone.pulse.registry.ListenerRegistry;
import org.bukkit.event.EventPriority;

@Singleton
public class BukkitQuitModule extends QuitModule {

    private final BukkitListenerRegistry bukkitListenerRegistry;

    @Inject
    public BukkitQuitModule(FileResolver fileResolver,
                            ListenerRegistry listenerRegistry,
                            IntegrationModule integrationModule,
                            BukkitListenerRegistry bukkitListenerRegistry) {
        super(fileResolver, listenerRegistry, integrationModule);

        this.bukkitListenerRegistry = bukkitListenerRegistry;
    }

    @Override
    public void reload() {
        super.reload();

        bukkitListenerRegistry.register(QuitListener.class, EventPriority.NORMAL);
    }
}
