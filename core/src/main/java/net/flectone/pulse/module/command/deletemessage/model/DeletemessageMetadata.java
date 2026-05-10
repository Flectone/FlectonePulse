package net.flectone.pulse.module.command.deletemessage.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.event.BaseEventMetadata;
import net.flectone.pulse.model.event.EventMetadata;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

@With
@Builder
public record DeletemessageMetadata<L extends LocalizationSetting>(
        @NonNull BaseEventMetadata<L> base,
        @NonNull UUID deletedUUID
) implements EventMetadata<L> {
}
