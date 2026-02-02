package net.flectone.pulse.module.integration.twitch.model;

import lombok.Builder;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.event.EventMetadata;
import org.jspecify.annotations.NonNull;

@Builder
public record TwitchMetadata<L extends LocalizationSetting>(
        @NonNull EventMetadata<L> base,
        @NonNull String nickname,
        @NonNull String channel
) implements EventMetadata<L> {}
