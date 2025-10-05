package net.flectone.pulse.module.integration.maintenance.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.StatusResponseEvent;
import net.flectone.pulse.model.event.module.ModuleEnableEvent;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.command.maintenance.MaintenanceModule;

@Singleton
public class MaintenancePulseListener implements PulseListener {

    private final net.flectone.pulse.module.integration.maintenance.MaintenanceModule maintenanceModule;

    @Inject
    public MaintenancePulseListener(net.flectone.pulse.module.integration.maintenance.MaintenanceModule maintenanceModule) {
        this.maintenanceModule = maintenanceModule;
    }

    @Pulse
    public void onModuleEnableEvent(ModuleEnableEvent event) {
        if (!maintenanceModule.isHooked()) return;

        AbstractModule eventModule = event.getModule();
        if (eventModule instanceof MaintenanceModule && maintenanceModule.config().isDisableFlectonepulseMaintenance()) {
            event.setCancelled(true);
        }
    }

    @Pulse
    public void onStatusResponseEvent(StatusResponseEvent event) {
        if (!maintenanceModule.isHooked()) return;
        if (!maintenanceModule.isMaintenance()) return;

        event.setCancelled(true);
    }

}
