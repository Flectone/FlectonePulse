package net.flectone.pulse.model.event;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.util.Moderation;
import org.jspecify.annotations.NonNull;

@With
@Builder
public record UnModerationMetadata<L extends LocalizationSetting>(
        @NonNull BaseEventMetadata<L> base,
        @NonNull Moderation unmoderation
) implements EventMetadata<L> {
}
