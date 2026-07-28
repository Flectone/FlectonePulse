package net.flectone.pulse.module.message.chat.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.BaseEventMetadata;
import net.flectone.pulse.model.event.EventMetadata;
import org.jspecify.annotations.NonNull;

@With
@Builder
public record ChatMetadata(
        @NonNull BaseEventMetadata base,
        @NonNull Chat chat
) implements EventMetadata {
}
