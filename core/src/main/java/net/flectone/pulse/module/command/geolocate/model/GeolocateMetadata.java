package net.flectone.pulse.module.command.geolocate.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.model.event.EventMetadata;

import java.util.List;

@Getter
@SuperBuilder
public class GeolocateMetadata<L extends Localization.Localizable> extends EventMetadata<L> {

    @NonNull
    private final List<String> response;

}
