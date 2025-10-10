package net.flectone.pulse.module.command.stream.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.module.command.stream.StreamModule;
import net.flectone.pulse.processing.context.MessageContext;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class StreamPulseListener implements PulseListener {

    private final StreamModule streamModule;

    @Pulse(priority = Event.Priority.HIGH)
    public void onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.getContext();

        streamModule.addTag(messageContext);
    }

}
