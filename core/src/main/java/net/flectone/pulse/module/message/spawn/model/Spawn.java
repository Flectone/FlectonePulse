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

    @Nullable
    @Builder.Default
    private final String angle = "0";

    @Nullable
    @Builder.Default
    private final String yaw = "0";

    @Nullable
    private final String world;

    @Nullable
    private final String players;

    @Nullable
    private final FEntity target;

}
