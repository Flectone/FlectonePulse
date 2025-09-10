package net.flectone.pulse.module.message.stop.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.stop.StopModule;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

@Singleton
public class StopPulseListener implements PulseListener {

    private final StopModule stopModule;

    @Inject
    public StopPulseListener(StopModule stopModule) {
        this.stopModule = stopModule;
    }

    @Pulse
    public void onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        if (event.getTranslationKey() != MinecraftTranslationKey.COMMANDS_STOP_SUCCESS) return;

        event.setCancelled(true);
        stopModule.send(event.getFPlayer());
    }

}