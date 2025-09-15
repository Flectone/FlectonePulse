package net.flectone.pulse.module.message.teleport.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import net.flectone.pulse.model.entity.FEntity;
import org.jetbrains.annotations.Nullable;

@Getter
@Builder
public class TeleportLocation {

    @Nullable
    private final String entities;

    @Nullable
    private final FEntity target;

    @NonNull
    private final String x;

    @NonNull
    private final String y;

    @NonNull
    private final String z;

}
