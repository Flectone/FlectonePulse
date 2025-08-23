package net.flectone.pulse.model.event.metadata;

import lombok.Getter;
import net.flectone.pulse.util.constant.MessageType;

import java.util.List;

@Getter
public class AutoMessageMetadata extends MessageMetadata {

    private final List<String> messages;

    public AutoMessageMetadata(List<String> messages) {
        super(MessageType.AUTO, messages.toString());

        this.messages = messages;
    }
}
