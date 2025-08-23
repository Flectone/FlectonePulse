package net.flectone.pulse.model.event.metadata;

import lombok.Getter;
import net.flectone.pulse.util.constant.MessageType;

@Getter
public class UpdateMessageMetadata extends MessageMetadata {

    private final String latestVersion;
    private final String currentVersion;

    public UpdateMessageMetadata(String latestVersion, String currentVersion) {
        super(MessageType.UPDATE, "");

        this.latestVersion = latestVersion;
        this.currentVersion = currentVersion;
    }
}
