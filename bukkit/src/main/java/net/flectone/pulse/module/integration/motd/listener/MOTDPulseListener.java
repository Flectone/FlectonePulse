package net.flectone.pulse.module.integration.motd.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.module.ModuleEnableEvent;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.command.maintenance.MaintenanceModule;
import net.flectone.pulse.module.integration.motd.MOTDModule;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MOTDPulseListener implements PulseListener {

    private final MOTDModule motdModule;

    @Pulse
    public Event onModuleEnableEvent(ModuleEnableEvent event) {
        if (!motdModule.isHooked()) return event;

        AbstractModule eventModule = event.module();
        if (eventModule instanceof MaintenanceModule && motdModule.config().disableFlectonepulseStatus()) {
            return event.withCancelled(true);
        }

        return event;
    }

}
