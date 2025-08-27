package net.flectone.pulse.model.event;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.model.util.Moderation;

@Getter
@SuperBuilder
public class ModerationMetadata<L extends Localization.Localizable> extends EventMetadata<L> {

    @NonNull
    private final Moderation moderation;

}
