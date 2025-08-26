package net.flectone.pulse.module.message.advancement.model;

import net.flectone.pulse.module.message.advancement.AdvancementModule;
import org.jetbrains.annotations.Nullable;

public record CommandAdvancement(AdvancementModule.Relation relation,
                                 String owner,
                                 @Nullable ChatAdvancement chatAdvancement,
                                 @Nullable String content) implements Advancement {

    public boolean isIncorrect() {
        return chatAdvancement == null && content == null;
    }

}
