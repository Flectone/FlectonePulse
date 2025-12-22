package net.flectone.pulse.model.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public record Range(int value, Type type) {

    private static final Map<Type, Range> DEFAULT_RANGES = EnumSet.allOf(Type.class).stream()
                    .filter(type -> type != Type.BLOCKS)
                    .collect(Collectors.toMap(
                            Function.identity(),
                            Range::new,
                            (a, b) -> a,
                            () -> new EnumMap<>(Type.class)
                    ));

    public Range {
        if (value < 0 && type == Type.BLOCKS) {
            throw new IllegalArgumentException("Block range cannot be negative: " + value);
        }
    }

    public Range(int value) {
        this(value, Type.BLOCKS);
    }

    public Range(Type type) {
        this(type.value, type);
    }

    public boolean is(Type type) {
        return this.type == type;
    }

    public static Range get(int range) {
        return new Range(range);
    }

    public static Range get(Type type) {
        if (type == Type.BLOCKS) {
            throw new IllegalArgumentException("You can't get default BLOCKS range");
        }

        return DEFAULT_RANGES.get(type);
    }

    @JsonValue
    public Object toJson() {
        if (this.type == Type.BLOCKS) {
            return this.value;
        }

        return this.type.name();
    }

    @JsonCreator
    public static Range fromJson(Object object) {
        String string = String.valueOf(object);

        try {
            int value = Integer.parseInt(string);
            Range.Type type = Range.Type.fromInt(value);
            if (type == Range.Type.BLOCKS) {
                return new Range(value);
            }
            return new Range(type);
        } catch (NumberFormatException e) {
            Range.Type type = Range.Type.fromString(string);
            return new Range(type);
        }
    }

    public enum Type {
        WORLD_TYPE(-4),
        WORLD_NAME(-3),
        PROXY(-2),
        SERVER(-1),
        PLAYER(0),
        BLOCKS(Integer.MIN_VALUE);

        private final int value;

        Type(int value) {
            this.value = value;
        }

        public static Type fromInt(Integer integer) {
            return Arrays.stream(Type.values())
                    .filter(enumType -> enumType.value == integer)
                    .findAny()
                    .orElse(Type.BLOCKS);
        }

        public static Type fromString(String string) {
            return Arrays.stream(Type.values())
                    .filter(enumType -> enumType != Type.BLOCKS)
                    .filter(enumType -> enumType.name().equalsIgnoreCase(string))
                    .findAny()
                    .orElseThrow(() -> new IllegalArgumentException("Unknown range type: " + string));
        }
    }
}