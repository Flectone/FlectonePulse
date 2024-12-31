package net.flectone.pulse.module.message.advancement.model;

import java.util.Arrays;

public record Advancement(String title, String description, Advancement.Type type) {

    public enum Type {
        TASK,
        GOAL,
        CHALLENGE;

        public static Type fromString(String type) {
            return Arrays.stream(Type.values())
                    .filter(t -> t.name().equalsIgnoreCase(type))
                    .findAny()
                    .orElse(null);
        }
    }
}
