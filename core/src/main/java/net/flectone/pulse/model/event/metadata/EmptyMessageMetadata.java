package net.flectone.pulse.model.event.metadata;

import lombok.Getter;
import net.flectone.pulse.util.constant.MessageType;

@Getter
public class EmptyMessageMetadata extends MessageMetadata {

    public EmptyMessageMetadata(MessageType messageType) {
        super(messageType,"");
    }
}
