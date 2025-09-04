package net.flectone.pulse.module.command.poll.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.module.command.poll.PollModule;

@Getter
@SuperBuilder
public class PollMetadata<L extends Localization.Localizable> extends EventMetadata<L> {

    @NonNull
    private final Poll poll;

    @NonNull
    private PollModule.Status status;

    @NonNull
    private PollModule.Action action;

}
