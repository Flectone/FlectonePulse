package net.flectone.pulse.module.message.clear.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import net.flectone.pulse.model.entity.FEntity;
import org.jetbrains.annotations.Nullable;

@Getter
@Builder
public class Clear {

    @NonNull
    private final String items;

    @Nullable
    private final FEntity target;

    @Nullable
    private final String players;

}
