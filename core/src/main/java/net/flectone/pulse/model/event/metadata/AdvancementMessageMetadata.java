package net.flectone.pulse.model.event.metadata;

import lombok.Getter;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.message.advancement.model.ChatAdvancement;
import net.flectone.pulse.util.constant.MessageType;

@Getter
public class AdvancementMessageMetadata extends MessageMetadata{

    private final ChatAdvancement advancement;

    public AdvancementMessageMetadata(ChatAdvancement advancement) {
        super(MessageType.ADVANCEMENT, "Player gained advancement");

        this.advancement = advancement;
    }
}
