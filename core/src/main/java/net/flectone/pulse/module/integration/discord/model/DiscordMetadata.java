package net.flectone.pulse.module.integration.discord.model;

import lombok.Builder;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.event.BaseEventMetadata;
import net.flectone.pulse.model.event.EventMetadata;
import org.jspecify.annotations.NonNull;

@Builder
public record DiscordMetadata<L extends LocalizationSetting>(
        @NonNull EventMetadata<L> base,
        @NonNull String globalName,
        @NonNull String nickname,
        @NonNull String displayName,
        @NonNull String userName
) implements EventMetadata<L> {}
