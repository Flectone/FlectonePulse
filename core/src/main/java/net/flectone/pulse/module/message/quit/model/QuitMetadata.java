package net.flectone.pulse.module.message.quit.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.BaseEventMetadata;
import net.flectone.pulse.model.event.VanishMetadata;
import org.jspecify.annotations.NonNull;

@With
@Builder
public record QuitMetadata(
        @NonNull BaseEventMetadata base,
        boolean fakeMessage,
        boolean vanished
) implements VanishMetadata {
}
