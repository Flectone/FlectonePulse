package net.flectone.pulse.module.message.greeting.model;

import lombok.Builder;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.event.BaseEventMetadata;
import net.flectone.pulse.model.event.EventMetadata;
import org.jspecify.annotations.NonNull;

import java.util.List;

@Builder
public record GreetingMetadata<L extends LocalizationSetting>(
        @NonNull EventMetadata<L> base,
        @NonNull List<String> pixels
) implements EventMetadata<L> {}
