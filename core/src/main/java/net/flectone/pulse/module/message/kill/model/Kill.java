package net.flectone.pulse.module.message.kill.model;

import lombok.Builder;
import lombok.Getter;
import net.flectone.pulse.model.entity.FEntity;
import org.jetbrains.annotations.Nullable;

@Getter
@Builder
public class Kill {

    @Nullable
    private final String entities;

    @Nullable
    private final FEntity target;

}
