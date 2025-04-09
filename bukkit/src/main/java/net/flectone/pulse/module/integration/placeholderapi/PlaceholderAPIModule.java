package net.flectone.pulse.module.integration.placeholderapi;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.configuration.Integration;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.module.AbstractModule;

@Singleton
public class PlaceholderAPIModule extends AbstractModule {

    private final Integration.Placeholderapi integration;
    private final Permission.Integration.Placeholderapi permission;

    private final PermissionChecker permissionChecker;
    private final PlaceholderAPIIntegration placeholderAPIIntegration;

    @Inject
    public PlaceholderAPIModule(FileManager fileManager,
                                PermissionChecker permissionChecker,
                                PlaceholderAPIIntegration placeholderAPIIntegration) {
        this.permissionChecker = permissionChecker;
        this.placeholderAPIIntegration = placeholderAPIIntegration;

        integration = fileManager.getIntegration().getPlaceholderapi();
        permission = fileManager.getPermission().getIntegration().getPlaceholderapi();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);
        registerPermission(permission.getUse());

        placeholderAPIIntegration.hook();
    }

    @Override
    protected boolean isConfigEnable() {
        return integration.isEnable();
    }

    public String setPlaceholders(FEntity sender, FEntity receiver, String message, boolean permission) {
        if (checkModulePredicates(sender)) return message;
        if (!permissionChecker.check(sender, this.permission.getUse()) && permission) return message;
        if (!permissionChecker.check(receiver, this.permission.getUse()) && permission) return message;

        return placeholderAPIIntegration.setPlaceholders(sender, receiver, message);
    }
}
