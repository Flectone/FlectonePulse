package net.flectone.pulse.module.integration.libertybans.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.module.ModuleEnableEvent;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.integration.libertybans.LibertyBansModule;
import net.flectone.pulse.platform.controller.ModuleController;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class LibertyBansPulseListener implements PulseListener {

    private final ModuleController moduleController;
    private final LibertyBansModule libertyBansModule;

    @Pulse
    public void onModuleEnableEvent(ModuleEnableEvent event) {
        if (!libertyBansModule.isHooked()) return;

        AbstractModule eventModule = event.getModule();
        Integration.Libertybans config = libertyBansModule.config();

        if ((config.isDisableFlectonepulseBan() && moduleController.isInstanceOfAny(eventModule, ModuleController.BAN_MODULES)) ||
                (config.isDisableFlectonepulseMute() && moduleController.isInstanceOfAny(eventModule, ModuleController.MUTE_MODULES)) ||
                (config.isDisableFlectonepulseWarn() && moduleController.isInstanceOfAny(eventModule, ModuleController.WARN_MODULES)) ||
                (config.isDisableFlectonepulseKick() && moduleController.isInstanceOfAny(eventModule, ModuleController.KICK_MODULES))) {
            event.setCancelled(true);
        }
    }

}
