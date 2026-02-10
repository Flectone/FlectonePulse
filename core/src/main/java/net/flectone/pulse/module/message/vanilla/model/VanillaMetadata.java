package net.flectone.pulse.module.message.vanilla.model;

import lombok.Builder;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.VanishMetadata;
import org.jspecify.annotations.NonNull;

@Builder
public record VanillaMetadata<L extends LocalizationSetting>(
        @NonNull EventMetadata<L> base,
        ParsedComponent parsedComponent,
        boolean ignoreVanish
) implements VanishMetadata<L> {}
