package net.flectone.pulse.module.command.mail.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.BaseEventMetadata;
import net.flectone.pulse.model.event.EventMetadata;
import org.jspecify.annotations.NonNull;

@With
@Builder
public record MailMetadata(
        @NonNull BaseEventMetadata base,
        @NonNull Mail mail,
        @NonNull FPlayer target
) implements EventMetadata {
}
