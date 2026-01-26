package net.flectone.pulse.module.command.coin.model;

import lombok.Builder;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.event.EventMetadata;
import org.jspecify.annotations.NonNull;

@Builder
public record CoinMetadata<L extends LocalizationSetting>(
        @NonNull EventMetadata<L> base,
        int percent
) implements EventMetadata<L> {}
