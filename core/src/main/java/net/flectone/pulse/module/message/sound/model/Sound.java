package net.flectone.pulse.module.message.sound.model;

import lombok.Builder;
import lombok.Getter;
import net.flectone.pulse.model.entity.FEntity;
import org.jetbrains.annotations.Nullable;


@Getter
@Builder
public class Sound {

    @Nullable
    private final String name;

    @Nullable
    private final String source;

    @Nullable
    private final String players;

    @Nullable
    private final FEntity target;

}
