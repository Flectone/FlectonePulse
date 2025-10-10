package net.flectone.pulse.module.message.stop.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.stop.StopModule;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class StopPulseListener implements PulseListener {

    private final StopModule stopModule;

    @Pulse
    public void onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        switch (event.getTranslationKey()) {
            case COMMANDS_STOP_SUCCESS, COMMANDS_STOP_START -> {
                event.setCancelled(true);
                stopModule.send(event.getFPlayer());
            }
        }
    }

}