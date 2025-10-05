package net.flectone.pulse.module.integration.placeholderapi;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;

@Singleton
public class PlaceholderAPIModule extends AbstractModule {

    private final FileResolver fileResolver;
    private final Provider<PlaceholderAPIIntegration> placeholderAPIIntegrationProvider;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public PlaceholderAPIModule(FileResolver fileResolver,
                                Provider<PlaceholderAPIIntegration> placeholderAPIIntegrationProvider,
                                ListenerRegistry listenerRegistry) {
        this.fileResolver = fileResolver;
        this.placeholderAPIIntegrationProvider = placeholderAPIIntegrationProvider;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        registerPermission(permission().getUse());

        placeholderAPIIntegrationProvider.get().hook();
        listenerRegistry.register(PlaceholderAPIIntegration.class);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        placeholderAPIIntegrationProvider.get().unhook();
    }

    @Override
    public Integration.Placeholderapi config() {
        return fileResolver.getIntegration().getPlaceholderapi();
    }

    @Override
    public Permission.Integration.Placeholderapi permission() {
        return fileResolver.getPermission().getIntegration().getPlaceholderapi();
    }
}
