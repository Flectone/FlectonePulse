package net.flectone.pulse.module.message.afk.model;

import lombok.Builder;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.VanishMetadata;
import org.jspecify.annotations.NonNull;

@Builder
public record AFKMetadata<L extends LocalizationSetting>(
        @NonNull EventMetadata<L> base,
        boolean newStatus,
        boolean ignoreVanish
) implements VanishMetadata<L> {}
