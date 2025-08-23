package net.flectone.pulse.module.message.update.model.metadata;

import net.flectone.pulse.model.event.EventMetadata;

public record UpdateMessageMetadata(String latestVersion, String currentVersion) implements EventMetadata {

}
