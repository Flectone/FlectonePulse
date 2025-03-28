package net.flectone.pulse.module.message.join;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.join.listener.JoinListener;
import net.flectone.pulse.registry.BukkitListenerRegistry;
import net.flectone.pulse.registry.ListenerRegistry;
import org.bukkit.event.EventPriority;

@Singleton
public class BukkitJoinModule extends JoinModule {

    private final BukkitListenerRegistry bukkitListenerRegistry;

    @Inject
    public BukkitJoinModule(FileManager fileManager,
                            ListenerRegistry listenerRegistry,
                            PlatformPlayerAdapter platformPlayerAdapter,
                            IntegrationModule integrationModule,
                            BukkitListenerRegistry bukkitListenerRegistry) {
        super(fileManager, listenerRegistry, platformPlayerAdapter, integrationModule);

        this.bukkitListenerRegistry = bukkitListenerRegistry;
    }


    @Override
    public void reload() {
        super.reload();

        bukkitListenerRegistry.register(JoinListener.class, EventPriority.NORMAL);
    }
}
