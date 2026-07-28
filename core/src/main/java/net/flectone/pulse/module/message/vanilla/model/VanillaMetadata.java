package net.flectone.pulse.module.message.vanilla.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.BaseEventMetadata;
import net.flectone.pulse.model.event.VanishMetadata;
import org.jspecify.annotations.NonNull;

@With
@Builder
public record VanillaMetadata(
        @NonNull BaseEventMetadata base,
        ParsedComponent parsedComponent,
        boolean fakeMessage,
        boolean vanished
) implements VanishMetadata {
}
