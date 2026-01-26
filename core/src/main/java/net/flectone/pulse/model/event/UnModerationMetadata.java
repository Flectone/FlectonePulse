package net.flectone.pulse.model.event;

import lombok.Builder;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Moderation;
import org.jspecify.annotations.NonNull;

import java.util.List;

@Builder
public record UnModerationMetadata<L extends LocalizationSetting>(
        @NonNull EventMetadata<L> base,
        @NonNull FPlayer moderator,
        @NonNull List<Moderation> moderations
) implements EventMetadata<L> {}
