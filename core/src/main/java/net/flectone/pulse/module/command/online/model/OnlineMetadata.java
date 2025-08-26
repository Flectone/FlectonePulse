package net.flectone.pulse.module.command.online.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.model.event.EventMetadata;

@Getter
@SuperBuilder
public class OnlineMetadata<M extends Localization.Localizable> extends EventMetadata<M> {

    @NonNull
    private final String type;

}
