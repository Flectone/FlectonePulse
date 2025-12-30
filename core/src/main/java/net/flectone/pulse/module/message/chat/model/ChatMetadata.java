package net.flectone.pulse.module.message.chat.model;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.event.EventMetadata;
import org.jspecify.annotations.NonNull;

@Getter
@SuperBuilder
public class ChatMetadata<L extends LocalizationSetting> extends EventMetadata<L> {

    @NonNull
    private final Chat chat;

}
