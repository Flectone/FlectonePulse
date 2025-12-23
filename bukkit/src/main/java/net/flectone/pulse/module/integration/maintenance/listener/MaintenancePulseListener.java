package net.flectone.pulse.module.integration.maintenance.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.StatusResponseEvent;
import net.flectone.pulse.model.event.module.ModuleEnableEvent;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.command.maintenance.MaintenanceModule;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MaintenancePulseListener implements PulseListener {

    private final net.flectone.pulse.module.integration.maintenance.MaintenanceModule maintenanceModule;

    @Pulse
    public Event onModuleEnableEvent(ModuleEnableEvent event) {
        if (!maintenanceModule.isHooked()) return event;

        AbstractModule eventModule = event.module();
        if (eventModule instanceof MaintenanceModule && maintenanceModule.config().disableFlectonepulseMaintenance()) {
            return event.withCancelled(true);
        }

        return event;
    }

    @Pulse
    public Event onStatusResponseEvent(StatusResponseEvent event) {
        if (!maintenanceModule.isHooked()) return event;
        if (!maintenanceModule.isMaintenance()) return event;

        return event.withCancelled(true);
    }

}
