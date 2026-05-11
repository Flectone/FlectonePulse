package net.flectone.pulse.module.command.whitelist.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.config.setting.LocalizationSetting;
import net.flectone.pulse.model.event.BaseEventMetadata;
import net.flectone.pulse.model.event.EventMetadata;
import org.jspecify.annotations.NonNull;

@With
@Builder
public record WhitelistMetadata<L extends LocalizationSetting>(
        @NonNull BaseEventMetadata<L> base,
        boolean turnedOn
) implements EventMetadata<L> {
}
