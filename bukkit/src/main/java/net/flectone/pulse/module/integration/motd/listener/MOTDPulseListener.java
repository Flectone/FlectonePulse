package net.flectone.pulse.module.integration.motd.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.module.ModuleEnableEvent;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.command.maintenance.MaintenanceModule;
import net.flectone.pulse.module.integration.motd.MOTDModule;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MOTDPulseListener implements PulseListener {

    private final MOTDModule motdModule;

    @Pulse
    public void onModuleEnableEvent(ModuleEnableEvent event) {
        if (!motdModule.isHooked()) return;

        AbstractModule eventModule = event.getModule();
        if (eventModule instanceof MaintenanceModule && motdModule.config().disableFlectonepulseStatus()) {
            event.setCancelled(true);
        }
    }

}
