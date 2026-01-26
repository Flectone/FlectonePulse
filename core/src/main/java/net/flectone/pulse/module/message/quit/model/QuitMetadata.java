package net.flectone.pulse.module.message.quit.model;

import lombok.Builder;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.event.EventMetadata;
import org.jspecify.annotations.NonNull;

@Builder
public record QuitMetadata<L extends LocalizationSetting>(
        @NonNull EventMetadata<L> base,
        boolean ignoreVanish
) implements EventMetadata<L> {}
