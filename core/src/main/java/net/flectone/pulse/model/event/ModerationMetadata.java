package net.flectone.pulse.model.event;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.util.Moderation;
import org.jspecify.annotations.NonNull;

@With
@Builder
public record ModerationMetadata(
        @NonNull BaseEventMetadata base,
        @NonNull Moderation moderation
) implements EventMetadata {
}
