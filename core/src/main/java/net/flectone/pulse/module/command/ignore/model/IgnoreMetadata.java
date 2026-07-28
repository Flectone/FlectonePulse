package net.flectone.pulse.module.command.ignore.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.BaseEventMetadata;
import net.flectone.pulse.model.event.EventMetadata;
import org.jspecify.annotations.NonNull;

@With
@Builder
public record IgnoreMetadata(
        @NonNull BaseEventMetadata base,
        @NonNull Ignore ignore,
        boolean ignored
) implements EventMetadata {
}
