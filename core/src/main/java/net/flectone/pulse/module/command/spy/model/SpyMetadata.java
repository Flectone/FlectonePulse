package net.flectone.pulse.module.command.spy.model;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.event.EventMetadata;
import org.jspecify.annotations.NonNull;

@Getter
@SuperBuilder
public class SpyMetadata<L extends LocalizationSetting> extends EventMetadata<L> {

    private final boolean turned;

    @NonNull
    private final String action;

}
