package net.flectone.pulse.module.message.effect.model;

import net.flectone.pulse.model.entity.FEntity;
import org.jetbrains.annotations.Nullable;

public record Effect(@Nullable String name, @Nullable FEntity target, @Nullable String count) {

    public boolean isIncorrect() {
        return target == null && count == null;
    }

}