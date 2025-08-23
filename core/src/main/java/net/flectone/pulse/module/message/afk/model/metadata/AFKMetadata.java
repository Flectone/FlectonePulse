package net.flectone.pulse.module.message.afk.model.metadata;

import net.flectone.pulse.model.event.EventMetadata;

public record AFKMetadata(boolean newStatus) implements EventMetadata {

}
