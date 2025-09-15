package net.flectone.pulse.module.message.spawn.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import net.flectone.pulse.model.entity.FEntity;
import org.jetbrains.annotations.Nullable;

@Getter
@Builder
public class Spawn {

    @NonNull
    private final String x;

    @NonNull
    private final String y;

    @NonNull
    private final String z;

    @NonNull
    private final String angle;

    @Nullable
    private final String world;

    @Nullable
    private final String players;

    @Nullable
    private final FEntity target;

}
