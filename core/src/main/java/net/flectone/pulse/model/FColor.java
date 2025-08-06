package net.flectone.pulse.model;

import java.util.Arrays;
import java.util.Optional;

public record FColor(int number, String name) {

    public enum Type {
        OUT,
        SEE;

        public static Optional<Type> fromString(String string) {
            return Arrays.stream(Type.values())
                    .filter(type -> type.name().equalsIgnoreCase(string))
                    .findFirst();
        }
    }

}
