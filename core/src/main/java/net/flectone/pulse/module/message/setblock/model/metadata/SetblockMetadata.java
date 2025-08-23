package net.flectone.pulse.module.message.setblock.model.metadata;

import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.module.message.setblock.model.Setblock;

public record SetblockMetadata(Setblock setblock) implements EventMetadata {

}
