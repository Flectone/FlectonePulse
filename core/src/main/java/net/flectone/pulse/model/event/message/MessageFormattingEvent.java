package net.flectone.pulse.model.event.message;

import lombok.With;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.event.Event;

@With
public record MessageFormattingEvent(
        boolean cancelled,
        MessageContext context
) implements Event {

    public MessageFormattingEvent(MessageContext context) {
        this(false, context);
    }

    public MessageFormattingEvent withContext(MessageContext context) {
        return this.context == context ? this : new MessageFormattingEvent(this.cancelled, context);
    }

}