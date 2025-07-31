package net.flectone.pulse.model.event.message;

import lombok.Getter;
import net.flectone.pulse.context.MessageContext;
import net.flectone.pulse.model.event.Event;

@Getter
public class MessageFormattingEvent extends Event {

    private final MessageContext context;

    public MessageFormattingEvent(MessageContext context) {
        this.context = context;
    }

}
