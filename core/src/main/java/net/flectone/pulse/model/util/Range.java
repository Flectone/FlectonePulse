package net.flectone.pulse.model.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

@Getter
public class Range {

    private static final Map<Type, Range> DEFAULT_RANGES = new EnumMap<>(Type.class);

    static {
        Arrays.stream(Type.values())
                .filter(enumType -> enumType != Type.BLOCKS)
                .forEach(enumType -> DEFAULT_RANGES.put(enumType, new Range(enumType)));
    }

    private final int value;
    private final Type type;

    public Range(int value) {
        if (value < 0) throw new IllegalArgumentException("Block range cannot be negative: " + value);

        this.value = value;
        this.type = Type.BLOCKS;
    }

    public Range(Type type) {
        this.value = type.value;
        this.type = type;
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
