package net.flectone.pulse.module.command.whitelist.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.event.BaseEventMetadata;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.util.Moderation;
import org.jspecify.annotations.NonNull;

@With
@Builder
public record WhitelistMetadata<L extends LocalizationSetting>(
        @NonNull BaseEventMetadata<L> base,
        Moderation moderation,
        boolean turnedOn
) implements EventMetadata<L> {
}
