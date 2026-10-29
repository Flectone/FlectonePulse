package net.flectone.pulse.checker;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.registry.PermissionRegistry;
import net.flectone.pulse.util.LazyInstance;

@Singleton
public class HytalePermissionChecker extends PermissionCheckerImpl {

    private final LazyInstance<IntegrationModule> integrationModule;

    @Inject
    public HytalePermissionChecker(PlatformPlayerAdapter platformPlayerAdapter,
                                   PermissionRegistry permissionRegistry,
                                   LazyInstance<IntegrationModule> integrationModule) {
        super(platformPlayerAdapter, integrationModule, permissionRegistry);

        this.integrationModule = integrationModule;
    }

    @Override
    public boolean checkPlatform(FPlayer fPlayer, String permission, boolean defaultValue) {
        if (integrationModule.get().containsEnabledChild(ModuleName.INTEGRATION_LUCKPERMS)) return defaultValue;

        return PermissionsModule.get().hasPermission(fPlayer.uuid(), permission, defaultValue);
    }

}
