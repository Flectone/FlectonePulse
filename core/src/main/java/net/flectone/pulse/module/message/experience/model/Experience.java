package net.flectone.pulse.module.message.experience.model;

import net.flectone.pulse.model.entity.FEntity;
import org.jetbrains.annotations.Nullable;

public record Experience(String amount, @Nullable String count, @Nullable FEntity target) {

    public boolean isIncorrect() {
        return count == null && target == null;
    }

}