package net.flectone.pulse.module.message.experience.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import net.flectone.pulse.model.entity.FEntity;
import org.jetbrains.annotations.Nullable;

@Getter
@Builder
public class Experience {

    @NonNull
    private final String amount;

    @Nullable
    private final String players;

    @Nullable
    private final FEntity target;

}