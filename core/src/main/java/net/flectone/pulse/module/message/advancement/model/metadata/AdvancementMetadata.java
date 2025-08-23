package net.flectone.pulse.module.message.advancement.model.metadata;

import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.module.message.advancement.model.ChatAdvancement;

public record AdvancementMetadata(ChatAdvancement advancement) implements EventMetadata {

}
