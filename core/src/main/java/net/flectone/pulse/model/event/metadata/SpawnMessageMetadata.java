package net.flectone.pulse.model.event.metadata;

import lombok.Getter;
import net.flectone.pulse.module.message.spawn.model.Spawn;
import net.flectone.pulse.util.constant.MessageType;

@Getter
public class SpawnMessageMetadata extends MessageMetadata {

    private final Spawn spawn;

    public SpawnMessageMetadata(Spawn spawn) {
        super(MessageType.SPAWN, "");

        this.spawn = spawn;
    }
}
