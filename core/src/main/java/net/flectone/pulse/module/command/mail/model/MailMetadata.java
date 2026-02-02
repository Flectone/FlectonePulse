package net.flectone.pulse.module.command.mail.model;

import lombok.Builder;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import org.jspecify.annotations.NonNull;

@Builder
public record MailMetadata<L extends LocalizationSetting>(
        @NonNull EventMetadata<L> base,
        @NonNull Mail mail,
        @NonNull FPlayer target
) implements EventMetadata<L> {}
