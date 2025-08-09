package net.flectone.pulse.module.integration.maintenance;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.status.StatusModule;
import net.flectone.pulse.processing.resolver.FileResolver;

@Singleton
public class MaintenanceModule extends AbstractModule {

    private final Integration.Maintenance integration;
    private final Permission.Integration.Maintenance permission;
    private final MaintenanceIntegration maintenanceIntegration;
    private final StatusModule statusModule;
    private final net.flectone.pulse.module.command.maintenance.MaintenanceModule maintenanceModule;

    @Inject
    public MaintenanceModule(FileResolver fileResolver,
                             MaintenanceIntegration maintenanceIntegration,
                             StatusModule statusModule,
                             net.flectone.pulse.module.command.maintenance.MaintenanceModule maintenanceModule) {
        this.integration = fileResolver.getIntegration().getMaintenance();
        this.permission = fileResolver.getPermission().getIntegration().getMaintenance();
        this.maintenanceIntegration = maintenanceIntegration;
        this.statusModule = statusModule;
        this.maintenanceModule = maintenanceModule;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        maintenanceIntegration.hook();

        statusModule.addPredicate(fPlayer -> maintenanceIntegration.isMaintenance());
        maintenanceModule.addPredicate(fPlayer -> integration.isDisableFlectonepulseMaintenance() && maintenanceIntegration.isHooked());
    }

    @Override
    public void onDisable() {
        maintenanceIntegration.unhook();
    }

    @Override
    protected boolean isConfigEnable() {
        return integration.isEnable();
    }
}
