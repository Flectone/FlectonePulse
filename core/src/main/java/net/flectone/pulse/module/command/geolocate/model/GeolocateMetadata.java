package net.flectone.pulse.module.command.geolocate.model;

import lombok.Builder;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.event.EventMetadata;
import org.jspecify.annotations.NonNull;

@Builder
public record GeolocateMetadata<L extends LocalizationSetting>(
        @NonNull EventMetadata<L> base,
        @NonNull IpResponse response
) implements EventMetadata<L> {}
