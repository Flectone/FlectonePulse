package net.flectone.pulse.module.command.coin.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.BaseEventMetadata;
import net.flectone.pulse.model.event.EventMetadata;
import org.jspecify.annotations.NonNull;

@With
@Builder
public record CoinMetadata(
        @NonNull BaseEventMetadata base,
        int percent
) implements EventMetadata {
}
