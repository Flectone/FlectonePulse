package net.flectone.pulse.module.integration.telegram.model;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.event.EventMetadata;
import org.jspecify.annotations.NonNull;

@Getter
@SuperBuilder
public class TelegramMetadata<L extends LocalizationSetting> extends EventMetadata<L> {

    @NonNull
    private final String userName;

    @NonNull
    private final String firstName;

    @NonNull
    private final String lastName;

    @NonNull
    private final String chat;

}
