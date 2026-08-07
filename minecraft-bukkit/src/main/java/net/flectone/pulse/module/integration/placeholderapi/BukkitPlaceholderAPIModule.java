package net.flectone.pulse.module.integration.placeholderapi;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.util.LazyInstance;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BukkitPlaceholderAPIModule implements ModuleSimple {

    private final FileFacade fileFacade;
    private final LazyInstance<BukkitPlaceholderAPIIntegration> placeholderAPIIntegration;
    private final ListenerRegistry listenerRegistry;

    @Override
    public void onEnable() {
        placeholderAPIIntegration.get().hook();
        listenerRegistry.register(BukkitPlaceholderAPIIntegration.class);
    }

    @Override
    public Set<PermissionSetting> permissions() {
        Set<PermissionSetting> permissions = new LinkedHashSet<>(ModuleSimple.super.permissions());
        permissions.add(permission().use());
        return Collections.unmodifiableSet(permissions);
    }

    @Override
    public void onDisable() {
        placeholderAPIIntegration.get().unhook();
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