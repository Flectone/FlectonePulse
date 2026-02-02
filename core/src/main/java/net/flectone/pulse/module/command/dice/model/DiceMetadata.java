package net.flectone.pulse.module.command.dice.model;

import lombok.Builder;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.event.EventMetadata;
import org.jspecify.annotations.NonNull;

import java.util.List;

@Builder
public record DiceMetadata<L extends LocalizationSetting>(
        @NonNull EventMetadata<L> base,
        @NonNull List<Integer> cubes
) implements EventMetadata<L> {}
