package net.flectone.pulse.module.message.join.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.BaseEventMetadata;
import net.flectone.pulse.model.event.VanishMetadata;
import org.jspecify.annotations.NonNull;

@With
@Builder
public record JoinMetadata(
        @NonNull BaseEventMetadata base,
        boolean playedBefore,
        boolean fakeMessage,
        boolean vanished
) implements VanishMetadata {
}
