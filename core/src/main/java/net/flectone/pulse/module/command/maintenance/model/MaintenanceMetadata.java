package net.flectone.pulse.module.command.maintenance.model;

import lombok.Builder;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.event.EventMetadata;
import org.jspecify.annotations.NonNull;

@Builder
public record MaintenanceMetadata<L extends LocalizationSetting>(
        @NonNull EventMetadata<L> base,
        boolean turned
) implements EventMetadata<L> {}
