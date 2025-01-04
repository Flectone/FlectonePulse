package net.flectone.pulse.module.integration.placeholderapi;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.file.Integration;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.util.PermissionUtil;

@Singleton
public class PlaceholderAPIModule extends AbstractModule {

    private final Integration.Placeholderapi integration;
    private final Permission.Integration.Placeholderapi permission;

    private final PermissionUtil permissionUtil;
    private final PlaceholderAPIIntegration placeholderAPIIntegration;

    @Inject
    public PlaceholderAPIModule(FileManager fileManager,
                                PermissionUtil permissionUtil,
                                PlaceholderAPIIntegration placeholderAPIIntegration) {
        this.permissionUtil = permissionUtil;
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
    public boolean isConfigEnable() {
        return integration.isEnable();
    }

    public String setPlaceholders(FEntity sender, FEntity receiver, String message, boolean permission) {
        if (checkModulePredicates(sender)) return message;
        if (!permissionUtil.has(sender, this.permission.getUse()) && permission) return message;
        if (!permissionUtil.has(receiver, this.permission.getUse()) && permission) return message;

        return placeholderAPIIntegration.setPlaceholders(sender, receiver, message);
    }
}
