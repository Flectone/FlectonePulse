package net.flectone.pulse.module.integration.placeholderapi;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Integration;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.registry.ListenerRegistry;
import net.flectone.pulse.resolver.FileResolver;

@Singleton
public class PlaceholderAPIModule extends AbstractModule {

    private final Integration.Placeholderapi integration;
    private final Permission.Integration.Placeholderapi permission;
    private final PlaceholderAPIIntegration placeholderAPIIntegration;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public PlaceholderAPIModule(FileResolver fileResolver,
                                PlaceholderAPIIntegration placeholderAPIIntegration,
                                ListenerRegistry listenerRegistry) {
        this.integration = fileResolver.getIntegration().getPlaceholderapi();
        this.permission = fileResolver.getPermission().getIntegration().getPlaceholderapi();
        this.placeholderAPIIntegration = placeholderAPIIntegration;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);
        registerPermission(permission.getUse());

        placeholderAPIIntegration.hook();

        listenerRegistry.register(PlaceholderAPIIntegration.class);
    }

    @Override
    public void onDisable() {
        placeholderAPIIntegration.unhook();
    }

    @Override
    protected boolean isConfigEnable() {
        return integration.isEnable();
    }
}
