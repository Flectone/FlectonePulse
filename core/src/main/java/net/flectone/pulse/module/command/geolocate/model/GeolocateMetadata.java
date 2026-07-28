package net.flectone.pulse.module.command.geolocate.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.BaseEventMetadata;
import net.flectone.pulse.model.event.EventMetadata;
import org.jspecify.annotations.NonNull;

@With
@Builder
public record GeolocateMetadata(
        @NonNull BaseEventMetadata base,
        @NonNull IpResponse response
) implements EventMetadata {
}
