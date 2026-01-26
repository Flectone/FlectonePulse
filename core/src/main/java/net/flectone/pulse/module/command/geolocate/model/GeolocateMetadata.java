package net.flectone.pulse.module.command.geolocate.model;

import lombok.Builder;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.event.BaseEventMetadata;
import net.flectone.pulse.model.event.EventMetadata;
import org.jspecify.annotations.NonNull;

import java.util.List;

@Builder
public record GeolocateMetadata<L extends LocalizationSetting>(
        @NonNull EventMetadata<L> base,
        @NonNull List<String> response
) implements EventMetadata<L> {}
