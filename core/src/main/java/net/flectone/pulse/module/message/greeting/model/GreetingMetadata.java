package net.flectone.pulse.module.message.greeting.model;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.event.EventMetadata;
import org.jspecify.annotations.NonNull;

import java.util.List;

@Getter
@SuperBuilder
public class GreetingMetadata<L extends LocalizationSetting> extends EventMetadata<L> {

    @NonNull
    private final List<String> pixels;

}
