package net.flectone.pulse.model.event.message;

import lombok.With;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.util.constant.ModuleName;
import net.kyori.adventure.text.Component;

@With
public record MessageSendEvent(
        boolean cancelled,
        ModuleName moduleName,
        Component message,
        Component submessage,
        EventMetadata eventMetadata,
        MessageContext messageContext
) implements Event {

    public MessageSendEvent(ModuleName moduleName, Component message, Component submessage, EventMetadata eventMetadata, MessageContext messageContext) {
        this(false, moduleName, message, submessage, eventMetadata, messageContext);
    }

}