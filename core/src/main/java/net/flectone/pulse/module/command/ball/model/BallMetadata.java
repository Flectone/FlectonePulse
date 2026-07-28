package net.flectone.pulse.module.command.ball.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.BaseEventMetadata;
import net.flectone.pulse.model.event.EventMetadata;
import org.jspecify.annotations.NonNull;

@With
@Builder
public record BallMetadata(
        @NonNull BaseEventMetadata base,
        int answer
) implements EventMetadata {
}
