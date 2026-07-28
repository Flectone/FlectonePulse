package net.flectone.pulse.module.message.update.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.BaseEventMetadata;
import net.flectone.pulse.model.event.EventMetadata;
import org.jspecify.annotations.NonNull;

@With
@Builder
public record UpdateMessageMetadata(
        @NonNull BaseEventMetadata base,
        @NonNull String latestVersion,
        @NonNull String currentVersion
) implements EventMetadata {
}
