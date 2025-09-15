package net.flectone.pulse.module.message.teleport.model;

import lombok.Builder;
import lombok.Getter;
import net.flectone.pulse.model.entity.FEntity;
import org.jetbrains.annotations.Nullable;

@Getter
@Builder
public class TeleportEntity {

    @Nullable
    private final String entities;

    @Nullable
    private final FEntity target;

    @Nullable
    private final FEntity secondTarget;

}
