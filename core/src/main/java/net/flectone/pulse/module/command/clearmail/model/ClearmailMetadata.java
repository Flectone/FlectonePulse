package net.flectone.pulse.module.command.clearmail.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.module.command.mail.model.Mail;

@Getter
@SuperBuilder
public class ClearmailMetadata<L extends Localization.Localizable> extends EventMetadata<L> {

    @NonNull
    private final Mail mail;

}
