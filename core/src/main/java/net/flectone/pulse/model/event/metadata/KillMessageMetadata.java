package net.flectone.pulse.model.event.metadata;

import lombok.Getter;
import net.flectone.pulse.module.message.kill.model.Kill;
import net.flectone.pulse.util.constant.MessageType;

@Getter
public class KillMessageMetadata extends MessageMetadata {

    private final Kill kill;

    public KillMessageMetadata(Kill kill) {
        super(MessageType.KILL, "");

        this.kill = kill;
    }
}
