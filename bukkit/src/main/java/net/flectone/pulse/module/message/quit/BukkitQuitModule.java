package net.flectone.pulse.module.message.quit;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.quit.listener.QuitListener;
import net.flectone.pulse.registry.BukkitListenerRegistry;
import net.flectone.pulse.registry.EventProcessRegistry;
import net.flectone.pulse.resolver.FileResolver;
import org.bukkit.event.EventPriority;

@Singleton
public class BukkitQuitModule extends QuitModule {

    private final BukkitListenerRegistry bukkitListenerRegistry;

    @Inject
    public BukkitQuitModule(FileResolver fileResolver,
                            IntegrationModule integrationModule,
                            BukkitListenerRegistry bukkitListenerRegistry,
                            EventProcessRegistry eventProcessRegistry) {
        super(fileResolver, integrationModule, eventProcessRegistry);

        this.bukkitListenerRegistry = bukkitListenerRegistry;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        bukkitListenerRegistry.register(QuitListener.class, EventPriority.NORMAL);
    }
}
