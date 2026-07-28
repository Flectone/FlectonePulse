package net.flectone.pulse.module.command.online.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.BaseEventMetadata;
import net.flectone.pulse.model.event.EventMetadata;
import org.jspecify.annotations.NonNull;

@With
@Builder
public record OnlineMetadata(
        @NonNull BaseEventMetadata base,
        @NonNull String type
) implements EventMetadata {
}
