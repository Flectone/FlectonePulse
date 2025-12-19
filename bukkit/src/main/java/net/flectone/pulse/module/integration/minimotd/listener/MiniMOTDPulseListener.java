package net.flectone.pulse.module.integration.minimotd.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.module.ModuleEnableEvent;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.command.maintenance.MaintenanceModule;
import net.flectone.pulse.module.integration.minimotd.MiniMOTDModule;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MiniMOTDPulseListener implements PulseListener {

    private final MiniMOTDModule miniMOTDModule;

    @Pulse
    public void onModuleEnableEvent(ModuleEnableEvent event) {
        if (!miniMOTDModule.isHooked()) return;

        AbstractModule eventModule = event.getModule();
        if (eventModule instanceof MaintenanceModule
                && miniMOTDModule.config().disableFlectonepulseStatus()
                && miniMOTDModule.isHooked()) {
            event.setCancelled(true);
        }
    }

}
