package net.flectone.pulse.module.command.stream.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.BaseEventMetadata;
import net.flectone.pulse.model.event.EventMetadata;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@With
@Builder
public record StreamMetadata(
        @NonNull BaseEventMetadata base,
        boolean turned,
        @Nullable String urls
) implements EventMetadata {
}
