package net.flectone.pulse.module.message.recipe.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import net.flectone.pulse.model.entity.FEntity;
import org.jetbrains.annotations.Nullable;

@Getter
@Builder
public class Recipe {

    @NonNull
    private String recipes;

    @Nullable
    private String players;

    @Nullable
    private FEntity target;

}
