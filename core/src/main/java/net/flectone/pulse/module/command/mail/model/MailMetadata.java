package net.flectone.pulse.module.command.mail.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.model.event.EventMetadata;

@Getter
@SuperBuilder
public class MailMetadata<L extends Localization.Localizable> extends EventMetadata<L> {

    @NonNull
    private final Mail mail;

}
