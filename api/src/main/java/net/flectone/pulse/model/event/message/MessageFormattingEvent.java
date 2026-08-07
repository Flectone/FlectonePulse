package net.flectone.pulse.model.event.message;

import lombok.With;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.context.MessageContext;

/**
 * Fired while a message is being turned into a component, so listeners can adjust the text or its tags.
 *
 * @param cancelled whether a listener vetoed the formatting
 * @param context the message being formatted
 * @author TheFaser
 */
@With
public record MessageFormattingEvent(
        boolean cancelled,
        MessageContext context
) implements Event {

    /**
     * Creates an event that has not been cancelled.
     *
     * @param context the message being formatted
     */
    public MessageFormattingEvent(MessageContext context) {
        this(false, context);
    }

    /**
     * Returns a copy carrying a different context, or this event if the context is unchanged.
     *
     * @param context the new context
     * @return the copy, or this instance when nothing changed
     */
    public MessageFormattingEvent withContext(MessageContext context) {
        return this.context == context ? this : new MessageFormattingEvent(this.cancelled, context);
    }

}