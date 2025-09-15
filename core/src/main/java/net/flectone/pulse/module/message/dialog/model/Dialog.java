package net.flectone.pulse.module.message.dialog.model;

import lombok.Builder;
import lombok.Getter;
import net.flectone.pulse.model.entity.FEntity;
import org.jetbrains.annotations.Nullable;

@Getter
@Builder
public class Dialog {

    @Nullable
    private final FEntity target;

    @Nullable
    private final String players;

}