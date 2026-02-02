package net.flectone.pulse.module.command.deletemessage.model;

import lombok.Builder;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.event.EventMetadata;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

@Builder
public record DeletemessageMetadata<L extends LocalizationSetting>(
        @NonNull EventMetadata<L> base,
        @NonNull UUID deletedUUID
) implements EventMetadata<L> {}
