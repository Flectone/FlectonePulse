package net.flectone.pulse.module.command.poll.model;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.module.command.poll.PollModule;
import org.jspecify.annotations.NonNull;

@Getter
@SuperBuilder
public class PollMetadata<L extends LocalizationSetting> extends EventMetadata<L> {

    @NonNull
    private final Poll poll;

    private PollModule.@NonNull Status status;

    private PollModule.@NonNull Action action;

}
