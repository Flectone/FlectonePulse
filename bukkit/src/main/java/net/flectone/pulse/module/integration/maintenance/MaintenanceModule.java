package net.flectone.pulse.module.integration.maintenance;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.integration.maintenance.listener.MaintenancePulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;

@Singleton
public class MaintenanceModule extends AbstractModule {

    private final FileResolver fileResolver;
    private final MaintenanceIntegration maintenanceIntegration;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public MaintenanceModule(FileResolver fileResolver,
                             MaintenanceIntegration maintenanceIntegration,
                             ListenerRegistry listenerRegistry) {
        this.fileResolver = fileResolver;
        this.maintenanceIntegration = maintenanceIntegration;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        maintenanceIntegration.hook();

        listenerRegistry.register(MaintenancePulseListener.class);
    }

    @Override
    public void onDisable() {
        super.onDisable();

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

    public boolean isHooked() {
        return maintenanceIntegration.isHooked();
    }

    public boolean isMaintenance() {
        return maintenanceIntegration.isMaintenance();
    }
}
