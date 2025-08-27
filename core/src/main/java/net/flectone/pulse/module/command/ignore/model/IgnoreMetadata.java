package net.flectone.pulse.module.command.ignore.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.model.event.EventMetadata;

@Getter
@SuperBuilder
public class IgnoreMetadata<L extends Localization.Localizable> extends EventMetadata<L> {

    @NonNull
    private final Ignore ignore;

    private final boolean ignored;

}
