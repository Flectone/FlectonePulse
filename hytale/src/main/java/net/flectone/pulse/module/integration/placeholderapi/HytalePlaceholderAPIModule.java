package net.flectone.pulse.module.integration.placeholderapi;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class HytalePlaceholderAPIModule extends AbstractModule {

    private final FileFacade fileFacade;
    private final Provider<HytalePlaceholderAPIIntegration> placeholderAPIIntegrationProvider;
    private final ListenerRegistry listenerRegistry;

    @Override
    public void onEnable() {
        super.onEnable();

        placeholderAPIIntegrationProvider.get().hook();
        listenerRegistry.register(HytalePlaceholderAPIIntegration.class);
    }

    @Override
    public ImmutableList.Builder<PermissionSetting> permissionBuilder() {
        return super.permissionBuilder().add(permission().use());
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
