package net.flectone.pulse.module.integration.interactivechat.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.module.ModuleEnableEvent;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.format.moderation.delete.DeleteModule;
import net.flectone.pulse.util.logging.FLogger;

@Singleton
public class InteractiveChatPulseListener implements PulseListener {

    private final FLogger fLogger;

    @Inject
    public InteractiveChatPulseListener(FLogger fLogger) {
        this.fLogger = fLogger;
    }

    @Pulse
    public void onModuleEnableEvent(ModuleEnableEvent event) {
        AbstractModule eventModule = event.getModule();
        if (eventModule instanceof DeleteModule) {
            fLogger.warning("Delete module is disabled, InteractiveChat is incompatible with it");
            event.setCancelled(true);
        }
    }
}
