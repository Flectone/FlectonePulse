package net.flectone.pulse.module.integration.twitch.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.event.EventMetadata;

@Getter
@SuperBuilder
public class TwitchMetadata<L extends LocalizationSetting> extends EventMetadata<L> {

    @NonNull
    private final String nickname;

    @NonNull
    private final String channel;

}
