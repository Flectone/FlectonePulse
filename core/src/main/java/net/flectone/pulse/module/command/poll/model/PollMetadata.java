package net.flectone.pulse.module.command.poll.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.model.event.EventMetadata;

@Getter
@SuperBuilder
public class PollMetadata<L extends Localization.Localizable> extends EventMetadata<L> {

    @NonNull
    private final Poll poll;

}
