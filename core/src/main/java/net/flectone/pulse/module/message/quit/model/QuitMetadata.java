package net.flectone.pulse.module.message.quit.model;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.model.event.EventMetadata;

@Getter
@SuperBuilder
public class QuitMetadata<L extends Localization.Localizable> extends EventMetadata<L> {

    private final boolean ignoreVanish;

}
