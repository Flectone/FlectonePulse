package net.flectone.pulse.model.event.metadata;

import lombok.Getter;
import net.flectone.pulse.module.message.setblock.model.Setblock;
import net.flectone.pulse.util.constant.MessageType;

@Getter
public class SetblockMessageMetadata extends MessageMetadata {

    private final Setblock setblock;

    public SetblockMessageMetadata(Setblock setblock) {
        super(MessageType.COMMAND_SETBLOCK, "");

        this.setblock = setblock;
    }
}
