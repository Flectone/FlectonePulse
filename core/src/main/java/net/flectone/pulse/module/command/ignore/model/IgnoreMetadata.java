package net.flectone.pulse.module.command.ignore.model;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.event.EventMetadata;
import org.jspecify.annotations.NonNull;

@Getter
@SuperBuilder
public class IgnoreMetadata<L extends LocalizationSetting> extends EventMetadata<L> {

    @NonNull
    private final Ignore ignore;

    private final boolean ignored;

}
