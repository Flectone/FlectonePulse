package net.flectone.pulse.model.event;

import lombok.Builder;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.util.Moderation;
import org.jspecify.annotations.NonNull;

@Builder
public record ModerationMetadata<L extends LocalizationSetting>(
        @NonNull EventMetadata<L> base,
        @NonNull Moderation moderation
) implements EventMetadata<L> {}
