package net.flectone.pulse.module.message.gamemode.model;

import lombok.Builder;
import lombok.Getter;
import net.flectone.pulse.model.entity.FEntity;
import org.jetbrains.annotations.Nullable;

@Getter
@Builder
public class Gamemode {

    @Nullable
    private final String name;

    @Nullable
    private final FEntity target;

}
