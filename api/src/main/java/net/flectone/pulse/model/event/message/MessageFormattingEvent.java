package net.flectone.pulse.model.event.message;

import lombok.With;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.context.MessageContext;
import org.jspecify.annotations.Nullable;

/**
 * Fired while a message is being turned into a component, so listeners can adjust the text or its tags.
 *
 * @param cancelled whether a listener vetoed the formatting
 * @param eventMetadata the metadata of the message
 * @param context the message being formatted
 * @author TheFaser
 */
@With
public record MessageFormattingEvent(
        boolean cancelled,
        @Nullable EventMetadata eventMetadata,
        MessageContext context
) implements Event {

    /**
     * Creates an event that has not been cancelled.
     *
     * @param context the message being formatted
     */
    public MessageFormattingEvent(MessageContext context) {
        this(false, null, context);
    }

    /**
     * Creates an event that has not been cancelled.
     *
     * @param cancelled whether a listener vetoed the formatting
     * @param context the message being formatted
     */
    public MessageFormattingEvent(boolean cancelled, MessageContext context) {
        this(cancelled, null, context);
    }

    /**
     * Creates an event that has not been cancelled.
     *
     * @param eventMetadata the metadata of the message
     * @param context the message being formatted
     */
    public MessageFormattingEvent(@Nullable EventMetadata eventMetadata, MessageContext context) {
        this(false, eventMetadata, context);
    }

    /**
     * Returns a copy carrying a different context, or this event if the context is unchanged.
     *
     * @param context the new context
     * @return the copy, or this instance when nothing changed
     */
    public MessageFormattingEvent withContext(MessageContext context) {
        return this.context == context ? this : new MessageFormattingEvent(this.cancelled, this.eventMetadata, context);
    }

}
