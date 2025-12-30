package net.flectone.pulse.module.integration.twitch.model;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.event.EventMetadata;
import org.jspecify.annotations.NonNull;

@Getter
@SuperBuilder
public class TwitchMetadata<L extends LocalizationSetting> extends EventMetadata<L> {

    @NonNull
    private final String nickname;

    @NonNull
    private final String channel;

}
