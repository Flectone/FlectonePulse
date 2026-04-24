package net.flectone.pulse.module.integration.maintenance;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.module.integration.maintenance.listener.BukkitPulseMaintenanceListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BukkitMaintenanceModule implements ModuleSimple {

    private final FileFacade fileFacade;
    private final BukkitMaintenanceIntegration maintenanceIntegration;
    private final ListenerRegistry listenerRegistry;

    @Override
    public void onEnable() {
        maintenanceIntegration.hook();

        listenerRegistry.register(BukkitPulseMaintenanceListener.class);
    }

    @Override
    public void onDisable() {
        maintenanceIntegration.unhook();
    }

    @Override
    public ModuleName name() {
        return ModuleName.INTEGRATION_MAINTENANCE;
    }

    @Override
    public Integration.Maintenance config() {
        return fileFacade.integration().maintenance();
    }

    @Override
    public Permission.Integration.Maintenance permission() {
        return fileFacade.permission().integration().maintenance();
    }

    public boolean isHooked() {
        return maintenanceIntegration.isHooked();
    }

    public boolean isMaintenance() {
        return maintenanceIntegration.isMaintenance();
    }
}
