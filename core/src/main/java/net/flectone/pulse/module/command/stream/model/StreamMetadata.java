package net.flectone.pulse.module.command.stream.model;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.model.event.EventMetadata;
import org.jetbrains.annotations.Nullable;

@Getter
@SuperBuilder
public class StreamMetadata<L extends Localization.Localizable> extends EventMetadata<L> {

    private final boolean turned;

    @Nullable
    private final String urls;

}
