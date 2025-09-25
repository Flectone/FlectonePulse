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

    private final FileResolver fileResolver;
    private final MaintenanceIntegration maintenanceIntegration;
    private final StatusModule statusModule;
    private final net.flectone.pulse.module.command.maintenance.MaintenanceModule maintenanceModule;

    @Inject
    public MaintenanceModule(FileResolver fileResolver,
                             MaintenanceIntegration maintenanceIntegration,
                             StatusModule statusModule,
                             net.flectone.pulse.module.command.maintenance.MaintenanceModule maintenanceModule) {
        this.fileResolver = fileResolver;
        this.maintenanceIntegration = maintenanceIntegration;
        this.statusModule = statusModule;
        this.maintenanceModule = maintenanceModule;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission());

        maintenanceIntegration.hook();

        statusModule.addPredicate(fPlayer -> maintenanceIntegration.isMaintenance());
        maintenanceModule.addPredicate(fPlayer -> config().isDisableFlectonepulseMaintenance() && maintenanceIntegration.isHooked());
    }

    @Override
    public void onDisable() {
        maintenanceIntegration.unhook();
    }

    @Override
    public Integration.Maintenance config() {
        return fileResolver.getIntegration().getMaintenance();
    }

    @Override
    public Permission.Integration.Maintenance permission() {
        return fileResolver.getPermission().getIntegration().getMaintenance();
    }
}
