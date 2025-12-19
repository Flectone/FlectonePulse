package net.flectone.pulse.module.message.vanilla.model;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.event.EventMetadata;

@Getter
@SuperBuilder
public class VanillaMetadata<L extends LocalizationSetting> extends EventMetadata<L> {

    private final ParsedComponent parsedComponent;

}
