package net.flectone.pulse.module.integration.telegram.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.BaseEventMetadata;
import net.flectone.pulse.model.event.EventMetadata;
import org.jspecify.annotations.NonNull;

@With
@Builder
public record TelegramMetadata(
        @NonNull BaseEventMetadata base,
        @NonNull String userName,
        @NonNull String firstName,
        @NonNull String lastName,
        @NonNull String chat
) implements EventMetadata {
}
