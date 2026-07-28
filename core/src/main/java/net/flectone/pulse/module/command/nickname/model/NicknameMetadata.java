package net.flectone.pulse.module.command.nickname.model;

import lombok.Builder;
import lombok.With;
import net.flectone.pulse.model.event.BaseEventMetadata;
import net.flectone.pulse.model.event.EventMetadata;
import org.jspecify.annotations.NonNull;

@With
@Builder
public record NicknameMetadata(
        @NonNull BaseEventMetadata base,
        @NonNull String nickname
) implements EventMetadata {
}
