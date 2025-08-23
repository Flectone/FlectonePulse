package net.flectone.pulse.model.event.metadata;

import lombok.Getter;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.util.constant.MessageType;

@Getter
public class MessageMetadata extends EventMetadata {

    private final MessageType messageType;
    /**
     * Message contents are only used when the user specifically types out a message
     */
    private final String messageContents;

    public MessageMetadata(MessageType messageType,
                           String messageContents) {
        this.messageType = messageType;
        this.messageContents = messageContents;
    }
}
