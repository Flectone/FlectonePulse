package net.flectone.pulse.module.command.spy.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.model.event.EventMetadata;

@Getter
@SuperBuilder
public class SpyMetadata<L extends Localization.Localizable> extends EventMetadata<L> {

    private final boolean turned;

    @NonNull
    private final String action;

}
