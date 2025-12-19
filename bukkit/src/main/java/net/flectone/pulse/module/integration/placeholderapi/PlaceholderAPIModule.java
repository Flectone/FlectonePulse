package net.flectone.pulse.module.integration.placeholderapi;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PlaceholderAPIModule extends AbstractModule {

    private final FileFacade fileFacade;
    private final Provider<PlaceholderAPIIntegration> placeholderAPIIntegrationProvider;
    private final ListenerRegistry listenerRegistry;

    @Override
    public void onEnable() {
        super.onEnable();
        registerPermission(permission().use());

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
        return fileFacade.integration().placeholderapi();
    }

    @Override
    public Permission.Integration.Placeholderapi permission() {
        return fileFacade.permission().integration().placeholderapi();
    }
}
