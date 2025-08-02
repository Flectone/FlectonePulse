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

    private final Integration.Placeholderapi integration;
    private final Permission.Integration.Placeholderapi permission;
    private final Provider<PlaceholderAPIIntegration> placeholderAPIIntegrationProvider;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public PlaceholderAPIModule(FileResolver fileResolver,
                                Provider<PlaceholderAPIIntegration> placeholderAPIIntegrationProvider,
                                ListenerRegistry listenerRegistry) {
        this.integration = fileResolver.getIntegration().getPlaceholderapi();
        this.permission = fileResolver.getPermission().getIntegration().getPlaceholderapi();
        this.placeholderAPIIntegrationProvider = placeholderAPIIntegrationProvider;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);
        registerPermission(permission.getUse());

        placeholderAPIIntegrationProvider.get().hook();
        listenerRegistry.register(PlaceholderAPIIntegration.class);
    }

    @Override
    public void onDisable() {
        placeholderAPIIntegrationProvider.get().unhook();
    }

    @Override
    protected boolean isConfigEnable() {
        return integration.isEnable();
    }
}
