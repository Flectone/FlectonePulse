package net.flectone.pulse.model.event;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Moderation;

import java.util.List;

@Getter
@SuperBuilder
public class UnModerationMetadata<L extends Localization.Localizable> extends EventMetadata<L> {

    @NonNull
    private final FPlayer moderator;

    @NonNull
    private final List<Moderation> moderations;

}
