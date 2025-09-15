package net.flectone.pulse.module.message.attribute.model;

import lombok.Builder;
import lombok.Getter;
import net.flectone.pulse.model.entity.FEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@Builder
public class Attribute {

    @NotNull
    private final FEntity target;

    @NotNull
    private final String name;

    @Nullable
    private final String modifier;

    @Nullable
    private final String value;

}