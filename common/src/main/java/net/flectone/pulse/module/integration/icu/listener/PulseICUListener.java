package net.flectone.pulse.module.integration.icu.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.integration.icu.ICUModule;
import net.flectone.pulse.constant.MessageFlag;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PulseICUListener implements PulseListener {

    private final ICUModule icuModule;

    @Pulse(priority = Event.Priority.LOW)
    public MessageFormattingEvent onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.context();
        if (!messageContext.isFlag(MessageFlag.PLAYER_MESSAGE) && !messageContext.isFlag(MessageFlag.ICU_MODULE)) return event;

        String convertedMessage = icuModule.process(messageContext.sender(), messageContext.receiver(), messageContext.message());
        if (!convertedMessage.equals(messageContext.message())) {
            messageContext = messageContext.withMessage(convertedMessage);
        }

        return event.context().equals(messageContext) ? event : event.withContext(messageContext);
    }

}
