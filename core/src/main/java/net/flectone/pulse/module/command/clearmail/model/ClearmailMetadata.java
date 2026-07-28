package net.flectone.pulse.module.command.clearmail.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.BaseEventMetadata;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.module.command.mail.model.Mail;
import org.jspecify.annotations.NonNull;

@With
@Builder
public record ClearmailMetadata(
        @NonNull BaseEventMetadata base,
        @NonNull Mail mail
) implements EventMetadata {
}
