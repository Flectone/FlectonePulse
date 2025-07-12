package net.flectone.pulse.module.message.join;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.join.listener.JoinListener;
import net.flectone.pulse.registry.BukkitListenerRegistry;
import net.flectone.pulse.registry.ListenerRegistry;
import net.flectone.pulse.service.FPlayerService;
import org.bukkit.event.EventPriority;

@Singleton
public class BukkitJoinModule extends JoinModule {

    private final BukkitListenerRegistry bukkitListenerRegistry;

    @Inject
    public BukkitJoinModule(FileResolver fileResolver,
                            ListenerRegistry listenerRegistry,
                            FPlayerService fPlayerService,
                            PlatformPlayerAdapter platformPlayerAdapter,
                            IntegrationModule integrationModule,
                            BukkitListenerRegistry bukkitListenerRegistry) {
        super(fileResolver, listenerRegistry, fPlayerService, platformPlayerAdapter, integrationModule);

        this.bukkitListenerRegistry = bukkitListenerRegistry;
    }


    @Override
    public void onEnable() {
        super.onEnable();

        bukkitListenerRegistry.register(JoinListener.class, EventPriority.NORMAL);
    }
}
