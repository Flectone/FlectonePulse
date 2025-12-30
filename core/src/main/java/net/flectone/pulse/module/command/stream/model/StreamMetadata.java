package net.flectone.pulse.module.command.stream.model;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.event.EventMetadata;
import org.jspecify.annotations.Nullable;

@Getter
@SuperBuilder
public class StreamMetadata<L extends LocalizationSetting> extends EventMetadata<L> {

    private final boolean turned;

    @Nullable
    private final String urls;

}
