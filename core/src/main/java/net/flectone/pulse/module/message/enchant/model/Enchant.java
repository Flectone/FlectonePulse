package net.flectone.pulse.module.message.enchant.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import net.flectone.pulse.model.entity.FEntity;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

@Getter
@Builder
public class Enchant {

    @NonNull
    private final Component name;

    @Nullable
    private final String players;

    @Nullable
    private final FEntity target;

}
