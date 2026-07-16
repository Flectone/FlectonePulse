package net.flectone.pulse.module.message.format.padding.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.module.message.format.padding.PaddingModule;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PulsePaddingListener implements PulseListener {

    private final PaddingModule paddingModule;

    @Pulse
    public Event onMessageFormattingEvent(MessageFormattingEvent event) {
        return event.withContext(paddingModule.addTag(event.context()));
    }

}
