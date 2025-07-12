package net.flectone.pulse.module.message.join;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.join.listener.JoinListener;
import net.flectone.pulse.registry.BukkitListenerRegistry;
import net.flectone.pulse.registry.EventProcessRegistry;
import net.flectone.pulse.resolver.FileResolver;
import org.bukkit.event.EventPriority;

@Singleton
public class BukkitJoinModule extends JoinModule {

    private final BukkitListenerRegistry bukkitListenerRegistry;

    @Inject
    public BukkitJoinModule(FileResolver fileResolver,
                            PlatformPlayerAdapter platformPlayerAdapter,
                            IntegrationModule integrationModule,
                            BukkitListenerRegistry bukkitListenerRegistry,
                            EventProcessRegistry eventProcessRegistry) {
        super(fileResolver, platformPlayerAdapter, integrationModule, eventProcessRegistry);

        this.bukkitListenerRegistry = bukkitListenerRegistry;
    }


    @Override
    public void onEnable() {
        super.onEnable();

        bukkitListenerRegistry.register(JoinListener.class, EventPriority.NORMAL);
    }
}
