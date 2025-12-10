package net.flectone.pulse.module.command.mute.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.module.command.mute.MuteModule;
import net.flectone.pulse.processing.context.MessageContext;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MutePulseListener implements PulseListener {

    private final MuteModule muteModule;

    @Pulse(priority = Event.Priority.HIGH)
    public void onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.getContext();

        muteModule.addTag(messageContext);
    }

}
