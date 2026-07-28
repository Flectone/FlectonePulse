package net.flectone.pulse.module.command.try_.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.BaseEventMetadata;
import net.flectone.pulse.model.event.EventMetadata;
import org.jspecify.annotations.NonNull;

@With
@Builder
public record TryMetadata(
        @NonNull BaseEventMetadata base,
        int percent
) implements EventMetadata {
}
