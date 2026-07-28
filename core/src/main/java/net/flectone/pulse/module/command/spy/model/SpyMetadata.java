package net.flectone.pulse.module.command.spy.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.BaseEventMetadata;
import net.flectone.pulse.model.event.EventMetadata;
import org.jspecify.annotations.NonNull;

@With
@Builder
public record SpyMetadata(
        @NonNull BaseEventMetadata base,
        boolean turned,
        @NonNull String action
) implements EventMetadata {
}
