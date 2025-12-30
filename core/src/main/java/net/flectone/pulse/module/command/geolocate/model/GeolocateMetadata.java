package net.flectone.pulse.module.command.geolocate.model;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.event.EventMetadata;
import org.jspecify.annotations.NonNull;

import java.util.List;

@Getter
@SuperBuilder
public class GeolocateMetadata<L extends LocalizationSetting> extends EventMetadata<L> {

    @NonNull
    private final List<String> response;

}
