package net.flectone.pulse.model.event.message;

import lombok.Getter;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.util.constant.MessageType;

@Getter
public class MessagePrepareEvent extends Event {

    private final MessageType messageType;
    private final String rawFormat;
    private final EventMetadata<?> eventMetadata;

    public MessagePrepareEvent(MessageType messageType, String rawFormat, EventMetadata<?> eventMetadata) {
        this.messageType = messageType;
        this.rawFormat = rawFormat;
        this.eventMetadata = eventMetadata;
    }

}
