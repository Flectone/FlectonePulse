package net.flectone.pulse.module.integration.advancedban.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.module.ModuleEnableEvent;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.integration.advancedban.AdvancedBanModule;
import net.flectone.pulse.platform.controller.ModuleController;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class AdvancedBanPulseListener implements PulseListener {

    private final ModuleController moduleController;
    private final AdvancedBanModule advancedBanModule;

    @Pulse
    public void onModuleEnableEvent(ModuleEnableEvent event) {
        if (!advancedBanModule.isHooked()) return;

        AbstractModule eventModule = event.getModule();
        Integration.Advancedban config = advancedBanModule.config();

        if ((config.disableFlectonepulseBan() && moduleController.isInstanceOfAny(eventModule, ModuleController.BAN_MODULES)) ||
                (config.disableFlectonepulseMute() && moduleController.isInstanceOfAny(eventModule, ModuleController.MUTE_MODULES)) ||
                (config.disableFlectonepulseWarn() && moduleController.isInstanceOfAny(eventModule, ModuleController.WARN_MODULES)) ||
                (config.disableFlectonepulseKick() && moduleController.isInstanceOfAny(eventModule, ModuleController.KICK_MODULES))) {
            event.setCancelled(true);
        }
    }

}
