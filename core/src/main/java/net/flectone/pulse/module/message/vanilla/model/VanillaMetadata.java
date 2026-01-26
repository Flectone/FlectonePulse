package net.flectone.pulse.module.message.vanilla.model;

import lombok.Builder;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.event.EventMetadata;
import org.jspecify.annotations.NonNull;

@Builder
public record VanillaMetadata<L extends LocalizationSetting>(
        @NonNull EventMetadata<L> base,
        ParsedComponent parsedComponent
) implements EventMetadata<L> {}
