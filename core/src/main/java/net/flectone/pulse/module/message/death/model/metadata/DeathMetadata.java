package net.flectone.pulse.module.message.death.model.metadata;

import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.module.message.death.model.Death;

public record DeathMetadata(Death death) implements EventMetadata {

}
