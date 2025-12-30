package net.flectone.pulse.module.command.online.model;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.event.EventMetadata;
import org.jspecify.annotations.NonNull;

@Getter
@SuperBuilder
public class OnlineMetadata<M extends LocalizationSetting> extends EventMetadata<M> {

    @NonNull
    private final String type;

}
