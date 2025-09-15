package net.flectone.pulse.module.message.advancement.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import net.flectone.pulse.model.entity.FEntity;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

@Getter
@Builder
public class Advancement {

    @NonNull
    private final Component advancementComponent;

    @Nullable
    private final FEntity target;

    @Nullable
    private final String players;

    @Nullable
    private final String advancements;

    @Nullable
    private final String criterion;

}
