package net.flectone.pulse.model.event.metadata;

import lombok.Getter;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.message.death.model.Death;
import net.flectone.pulse.util.constant.MessageType;
import org.jetbrains.annotations.Nullable;

@Getter
public class DeathMessageMetadata extends MessageMetadata {

    private final Death death;

    public DeathMessageMetadata(Death death) {
        super(MessageType.DEATH, "");

        this.death = death;
    }
}
