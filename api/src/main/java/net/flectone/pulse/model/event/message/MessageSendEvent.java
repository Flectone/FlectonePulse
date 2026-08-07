package net.flectone.pulse.model.event.message;

import lombok.With;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.constant.ModuleName;
import net.kyori.adventure.text.Component;

/**
 * Fired for each receiver as the finished message goes out.
 *
 * @param cancelled whether a listener suppressed this delivery
 * @param moduleName the module the message came from
 * @param message the main text
 * @param submessage the secondary text, used by titles and toasts
 * @param eventMetadata how the message is delivered
 * @param messageContext the message context for this receiver
 * @author TheFaser
 */
@With
public record MessageSendEvent(
        boolean cancelled,
        ModuleName moduleName,
        Component message,
        Component submessage,
        EventMetadata eventMetadata,
        MessageContext messageContext
) implements Event {

    /**
     * Creates an event that has not been cancelled.
     *
     * @param moduleName the module the message came from
     * @param message the main text
     * @param submessage the secondary text
     * @param eventMetadata how the message is delivered
     * @param messageContext the message context for this receiver
     */
    public MessageSendEvent(ModuleName moduleName, Component message, Component submessage, EventMetadata eventMetadata, MessageContext messageContext) {
        this(false, moduleName, message, submessage, eventMetadata, messageContext);
    }

}