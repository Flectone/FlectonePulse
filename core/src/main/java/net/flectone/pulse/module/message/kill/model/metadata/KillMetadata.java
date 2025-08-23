package net.flectone.pulse.module.message.kill.model.metadata;

import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.module.message.kill.model.Kill;

public record KillMetadata(Kill kill) implements EventMetadata {

}
