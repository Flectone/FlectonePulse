package net.flectone.pulse.module.message.format.names.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.message.format.names.NamesModule;
import net.flectone.pulse.util.constant.MessageFlag;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class NamesPulseListener implements PulseListener {

    private final NamesModule namesModule;

    @Pulse(priority = Event.Priority.HIGH)
    public Event onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.context();
        if (messageContext.isFlag(MessageFlag.USER_MESSAGE)) return event;

        FEntity sender = messageContext.sender();
        if (messageContext.isFlag(MessageFlag.INVISIBLE_NAME) && namesModule.isInvisible(sender)) {
            return event.withContext(namesModule.addInvisibleTag(messageContext));
        } else {
            return event.withContext(namesModule.addTags(messageContext));
        }
    }
}
