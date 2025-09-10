package net.flectone.pulse.module.message.give.model;

import net.flectone.pulse.model.entity.FEntity;
import org.jetbrains.annotations.Nullable;

public record Give(String amount, String item, @Nullable FEntity target, @Nullable String count) {

    public boolean isIncorrect() {
        return target == null && count == null;
    }

}
