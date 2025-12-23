package net.flectone.pulse.model.event.message;

import lombok.With;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.util.constant.MessageType;

@With
public record MessagePrepareEvent(
        boolean cancelled,
        MessageType messageType,
        String rawFormat,
        EventMetadata<?> eventMetadata
) implements Event {

    public MessagePrepareEvent(MessageType messageType, String rawFormat, EventMetadata<?> eventMetadata) {
        this(false, messageType, rawFormat, eventMetadata);
    }

}