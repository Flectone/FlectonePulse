package net.flectone.pulse.module.command.nickname.model;

import lombok.Builder;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.event.EventMetadata;
import org.jspecify.annotations.NonNull;

@Builder
public record NicknameMetadata<L extends LocalizationSetting>(
        @NonNull EventMetadata<L> base,
        @NonNull String nickname
) implements EventMetadata<L> {}
