package net.flectone.pulse.module.message.give.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import net.flectone.pulse.model.entity.FEntity;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

@Getter
@Builder
public class Give {

    @NonNull
    private final String items;

    @NonNull
    private final Component item;

    @Nullable
    private final FEntity target;

    @Nullable
    private final String players;

}
