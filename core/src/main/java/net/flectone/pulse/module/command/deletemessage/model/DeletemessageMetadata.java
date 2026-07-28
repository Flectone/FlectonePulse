package net.flectone.pulse.module.command.deletemessage.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.BaseEventMetadata;
import net.flectone.pulse.model.event.EventMetadata;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

@With
@Builder
public record DeletemessageMetadata(
        @NonNull BaseEventMetadata base,
        @NonNull UUID deletedUUID
) implements EventMetadata {
}
