package net.flectone.pulse.module.message.format.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.message.format.convertor.LegacyColorConvertor;
import net.flectone.pulse.util.constant.MessageFlag;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class LegacyColorPulseListener implements PulseListener {

    private final LegacyColorConvertor legacyColorConvertor;

    @Pulse(priority = Event.Priority.HIGHEST)
    public Event onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.context();
        if (!messageContext.isFlag(MessageFlag.LEGACY_COLORS)) return event;

        return event.withContext(legacyColorConvertor.convert(messageContext));
    }
}