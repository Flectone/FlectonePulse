package net.flectone.pulse.module.command.dice.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.BaseEventMetadata;
import net.flectone.pulse.model.event.EventMetadata;
import org.jspecify.annotations.NonNull;

import java.util.List;

@With
@Builder
public record DiceMetadata(
        @NonNull BaseEventMetadata base,
        @NonNull List<Integer> cubes
) implements EventMetadata {
}
