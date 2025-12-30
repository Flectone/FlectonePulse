package net.flectone.pulse.model.event;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.util.Moderation;
import org.jspecify.annotations.NonNull;

@Getter
@SuperBuilder
public class ModerationMetadata<L extends LocalizationSetting> extends EventMetadata<L> {

    @NonNull
    private final Moderation moderation;

}
