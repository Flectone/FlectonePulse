package net.flectone.pulse.module.command.deletemessage.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.model.event.EventMetadata;

import java.util.UUID;

@Getter
@SuperBuilder
public class DeletemessageMetadata<L extends Localization.Localizable> extends EventMetadata<L> {

    @NonNull
    private final UUID deletedUUID;

}
