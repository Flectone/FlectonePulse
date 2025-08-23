package net.flectone.pulse.model.event.metadata;

import lombok.Getter;
import net.flectone.pulse.util.constant.MessageType;

@Getter
public class AFKMessageMetadata extends MessageMetadata{

    private final Boolean newStatus;

    public AFKMessageMetadata(Boolean newStatus) {
        super(MessageType.AFK, "New AFK status: " + newStatus);

        this.newStatus = newStatus;
    }
}
