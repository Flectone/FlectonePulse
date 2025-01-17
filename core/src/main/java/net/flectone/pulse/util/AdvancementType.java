package net.flectone.pulse.util;

import java.util.Arrays;

public enum AdvancementType {
    GOAL,
    TASK,
    CHALLENGE;

    public static AdvancementType fromString(String type) {
        return Arrays.stream(AdvancementType.values())
                .filter(t -> t.name().equalsIgnoreCase(type))
                .findAny()
                .orElse(null);
    }
}
