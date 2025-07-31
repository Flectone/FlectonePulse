package net.flectone.pulse.module.message.quit;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.quit.listener.QuitListener;
import net.flectone.pulse.registry.ListenerRegistry;
import net.flectone.pulse.resolver.FileResolver;

@Singleton
public class BukkitQuitModule extends QuitModule {

    private final ListenerRegistry listenerRegistry;

    @Inject
    public BukkitQuitModule(FileResolver fileResolver,
                            IntegrationModule integrationModule,
                            ListenerRegistry listenerRegistry) {
        super(fileResolver, integrationModule, listenerRegistry);

        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        listenerRegistry.register(QuitListener.class);
    }
}
