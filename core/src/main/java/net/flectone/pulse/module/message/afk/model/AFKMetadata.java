package net.flectone.pulse.module.message.afk.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.BaseEventMetadata;
import net.flectone.pulse.model.event.VanishMetadata;
import org.jspecify.annotations.NonNull;

@With
@Builder
public record AFKMetadata(
        @NonNull BaseEventMetadata base,
        boolean newStatus,
        boolean fakeMessage,
        boolean vanished
) implements VanishMetadata {
}
