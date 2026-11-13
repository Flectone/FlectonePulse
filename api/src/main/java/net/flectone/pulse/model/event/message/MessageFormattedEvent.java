package net.flectone.pulse.model.event.message;

import lombok.With;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

/**
 * Fired after a message has been turned into a component.
 * <p>
 * Unlike {@link MessageFormattingEvent}, this event is meant for observing the final
 * result, for example to remember components that were already rendered by the plugin.
 * </p>
 *
 * @param cancelled whether a listener vetoed the result
 * @param eventMetadata the metadata of the message
 * @param context the message that was formatted
 * @param component the resulting component
 * @author TheFaser
 */
@With
public record MessageFormattedEvent(
        boolean cancelled,
        @Nullable EventMetadata eventMetadata,
        MessageContext context,
        Component component
) implements Event {

    /**
     * Creates an event that has not been cancelled.
     *
     * @param context the message that was formatted
     * @param component the resulting component
     */
    public MessageFormattedEvent(MessageContext context, Component component) {
        this(false, null, context, component);
    }

    /**
     * Creates an event that has not been cancelled.
     *
     * @param eventMetadata the metadata of the message
     * @param context the message that was formatted
     * @param component the resulting component
     */
    public MessageFormattedEvent(@Nullable EventMetadata eventMetadata, MessageContext context, Component component) {
        this(false, eventMetadata, context, component);
    }

}
