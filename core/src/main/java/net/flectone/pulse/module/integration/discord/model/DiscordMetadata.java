package net.flectone.pulse.module.integration.discord.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.event.BaseEventMetadata;
import net.flectone.pulse.model.event.EventMetadata;
import org.jspecify.annotations.NonNull;

@With
@Builder
public record DiscordMetadata<L extends LocalizationSetting>(
        @NonNull BaseEventMetadata<L> base,
        @NonNull String globalName,
        @NonNull String nickname,
        @NonNull String displayName,
        @NonNull String userName
) implements EventMetadata<L> {
}
