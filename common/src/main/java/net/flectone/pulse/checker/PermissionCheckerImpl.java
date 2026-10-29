package net.flectone.pulse.checker;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.registry.PermissionRegistry;
import net.flectone.pulse.util.LazyInstance;
import org.jspecify.annotations.Nullable;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PermissionCheckerImpl implements PermissionChecker {

    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final LazyInstance<IntegrationModule> integrationModule;
    private final PermissionRegistry permissionRegistry;

    @Override
    public boolean check(FEntity entity, String permission) {
        if (permission == null) return true;
        if (!(entity instanceof FPlayer fPlayer) || fPlayer.isConsole()) return true;

        IntegrationModule integrationModuleInstance = integrationModule.get();

        Boolean integrationPermission = integrationModuleInstance.getFPlayerPermission(fPlayer, permission);
        if (Boolean.TRUE.equals(integrationPermission)) return true;

        Permission.Type type = getType(permission);
        if (type == Permission.Type.TRUE && integrationModuleInstance.isAlwaysHaveTruePermission()) return true;
        if (Boolean.FALSE.equals(integrationPermission)) return false;

        boolean defaultValue = getDefaultValue(fPlayer, type);
        return checkPlatform(fPlayer, permission, defaultValue);
    }

    public Permission.@Nullable Type getType(String permission) {
        return permissionRegistry.getType(permission);
    }

    public boolean checkPlatform(FPlayer fPlayer, String permission, boolean defaultValue) {
        return defaultValue;
    }

    private boolean getDefaultValue(FPlayer fPlayer, Permission.@Nullable Type type) {
        if (type == null) return platformPlayerAdapter.isOperator(fPlayer);

        return switch (type) {
            case TRUE -> true;
            case FALSE -> false;
            case OP -> platformPlayerAdapter.isOperator(fPlayer);
            case NOT_OP -> !platformPlayerAdapter.isOperator(fPlayer);
        };
    }

}
