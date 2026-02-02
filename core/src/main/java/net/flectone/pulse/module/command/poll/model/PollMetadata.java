package net.flectone.pulse.module.command.poll.model;

import lombok.Builder;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.module.command.poll.PollModule;
import org.jspecify.annotations.NonNull;

@Builder
public record PollMetadata<L extends LocalizationSetting>(
        @NonNull EventMetadata<L> base,
        @NonNull Poll poll,
        PollModule.@NonNull Status status,
        PollModule.@NonNull Action action
) implements EventMetadata<L> {}
