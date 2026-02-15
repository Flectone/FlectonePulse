package net.flectone.pulse.module.message.format.replacement.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.module.message.format.replacement.ReplacementModule;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.util.constant.MessageFlag;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ReplacementPulseListener implements PulseListener {

    private final ReplacementModule replacementModule;

    @Pulse
    public Event onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.context();
        if (!messageContext.isFlag(MessageFlag.REPLACEMENT)) return event;

        messageContext = replacementModule.format(messageContext);
        messageContext = replacementModule.addTags(messageContext);
        return event.withContext(messageContext);
    }

}
