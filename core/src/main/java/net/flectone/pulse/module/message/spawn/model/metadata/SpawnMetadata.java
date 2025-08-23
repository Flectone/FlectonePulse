package net.flectone.pulse.module.message.spawn.model.metadata;

import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.module.message.spawn.model.Spawn;

public record SpawnMetadata(Spawn spawn) implements EventMetadata {

}
