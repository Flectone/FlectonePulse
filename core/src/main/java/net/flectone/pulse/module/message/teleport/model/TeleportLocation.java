package net.flectone.pulse.module.message.teleport.model;

import net.flectone.pulse.model.entity.FEntity;
import org.jetbrains.annotations.Nullable;

public record TeleportLocation(@Nullable FEntity target, @Nullable String count, String x, String y, String z) {

    public boolean isIncorrect() {
        return target == null && count == null;
    }

}
