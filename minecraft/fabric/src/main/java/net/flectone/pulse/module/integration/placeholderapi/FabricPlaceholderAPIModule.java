package net.flectone.pulse.module.integration.placeholderapi;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;
import org.jspecify.annotations.NonNull;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FabricPlaceholderAPIModule implements ModuleSimple {

    private final FileFacade fileFacade;
    private final FabricPlaceholderAPIIntegration placeholderAPIIntegration;
    private final ListenerRegistry listenerRegistry;

    @Override
    public void onEnable() {
        placeholderAPIIntegration.hook();

        listenerRegistry.register(FabricPlaceholderAPIIntegration.class);
    }

    @Override
    public ImmutableSet.Builder<@NonNull PermissionSetting> permissionBuilder() {
        return ModuleSimple.super.permissionBuilder().add(permission().use());
    }

    @Override
    public void onDisable() {
        placeholderAPIIntegration.unhook();
    }

    @Override
    public ModuleName name() {
        return ModuleName.INTEGRATION_PLACEHOLDERAPI;
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
