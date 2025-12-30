package net.flectone.pulse.module.integration.placeholderapi;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.util.file.FileFacade;
import org.jspecify.annotations.NonNull;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PlaceholderAPIModule extends AbstractModule {

    private final FileFacade fileFacade;
    private final PlaceholderAPIIntegration placeholderAPIIntegration;
    private final ListenerRegistry listenerRegistry;

    @Override
    public void onEnable() {
        super.onEnable();

        placeholderAPIIntegration.hook();

        listenerRegistry.register(PlaceholderAPIIntegration.class);
    }

    @Override
    public ImmutableList.Builder<@NonNull PermissionSetting> permissionBuilder() {
        return super.permissionBuilder().add(permission().use());
    }

    @Override
    public void onDisable() {
        super.onDisable();

        placeholderAPIIntegration.unhook();
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
