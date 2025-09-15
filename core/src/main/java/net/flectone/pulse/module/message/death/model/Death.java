package net.flectone.pulse.module.message.death.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import net.flectone.pulse.model.entity.FEntity;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

@Getter
@Builder
public class Death {

    @NonNull
    private final FEntity target;

    @Nullable
    private final FEntity killer;

    @Nullable
    private final Component killerItem;

}
