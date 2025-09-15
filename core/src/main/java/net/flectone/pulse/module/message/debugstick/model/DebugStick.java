package net.flectone.pulse.module.message.debugstick.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

@Getter
@Builder
public class DebugStick {

    @NonNull
    private final String property;

    @Nullable
    private final String value;

}