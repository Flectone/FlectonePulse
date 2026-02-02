package net.flectone.pulse.module.command.clearmail.model;

import lombok.Builder;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.module.command.mail.model.Mail;
import org.jspecify.annotations.NonNull;

@Builder
public record ClearmailMetadata<L extends LocalizationSetting>(
        @NonNull EventMetadata<L> base,
        @NonNull Mail mail
) implements EventMetadata<L> {}
