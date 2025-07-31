package net.flectone.pulse.module.message.join;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.join.listener.JoinListener;
import net.flectone.pulse.registry.ListenerRegistry;
import net.flectone.pulse.resolver.FileResolver;

@Singleton
public class BukkitJoinModule extends JoinModule {

    private final ListenerRegistry listenerRegistry;

    @Inject
    public BukkitJoinModule(FileResolver fileResolver,
                            PlatformPlayerAdapter platformPlayerAdapter,
                            IntegrationModule integrationModule,
                            ListenerRegistry listenerRegistry) {
        super(fileResolver, platformPlayerAdapter, integrationModule, listenerRegistry);

        this.listenerRegistry = listenerRegistry;
    }


    @Override
    public void onEnable() {
        super.onEnable();

        listenerRegistry.register(JoinListener.class);
    }
}
