package net.flectone.pulse.module.integration.discord.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.event.EventMetadata;
import org.jetbrains.annotations.NotNull;

@Getter
@SuperBuilder
public class DiscordMetadata<L extends LocalizationSetting> extends EventMetadata<L> {

    @NonNull
    private final String globalName;

    @NonNull
    private final String nickname;

    @NonNull
    private final String displayName;

    @NotNull
    private final String userName;

}
