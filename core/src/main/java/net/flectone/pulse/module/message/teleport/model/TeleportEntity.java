package net.flectone.pulse.module.message.teleport.model;

import net.flectone.pulse.model.entity.FEntity;
import org.jetbrains.annotations.Nullable;

public record TeleportEntity(@Nullable FEntity target, @Nullable String count, FEntity destination) {

    public boolean isIncorrect() {
        return target == null && count == null;
    }

}
