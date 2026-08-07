package net.flectone.pulse.model.value;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * How far a message travels, either a radius in blocks or one of the named scopes
 * such as the whole server or the entire proxy network.
 *
 * @param value the block radius, or the marker value of the named type
 * @param type which of the two forms this range takes
 * @author TheFaser
 */
public record Range(int value, Type type) {

    private static final Map<Type, Range> DEFAULT_RANGES = EnumSet.allOf(Type.class).stream()
            .filter(type -> type != Type.BLOCKS)
            .collect(Collectors.toMap(
                    Function.identity(),
                    Range::new,
                    (a, _) -> a,
                    () -> new EnumMap<>(Type.class)
            ));

    public Range {
        if (value < 0 && type == Type.BLOCKS) {
            throw new IllegalArgumentException("Block range cannot be negative: " + value);
        }
    }

    /**
     * Creates a range covering the given radius in blocks.
     *
     * @param value the radius in blocks, must not be negative
     */
    public Range(int value) {
        this(value, Type.BLOCKS);
    }

    /**
     * Creates a range for one of the named scopes.
     *
     * @param type the named scope
     */
    public Range(Type type) {
        this(type.value, type);
    }

    /**
     * Returns a range covering the given radius in blocks.
     *
     * @param range the radius in blocks
     * @return the block range
     */
    public static Range get(int range) {
        return new Range(range);
    }

    /**
     * Returns the shared instance for a named scope.
     *
     * @param type the named scope, must not be {@link Type#BLOCKS}
     * @return the shared range for that scope
     * @throws IllegalArgumentException if {@link Type#BLOCKS} is passed, as it has no single default
     */
    public static Range get(Type type) {
        if (type == Type.BLOCKS) {
            throw new IllegalArgumentException("You can't get default BLOCKS range");
        }

        return DEFAULT_RANGES.get(type);
    }

    /**
     * Reads a range from its config representation, accepting either a block count or a scope name.
     *
     * @param object the raw config value
     * @return the parsed range
     */
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
        } catch (NumberFormatException _) {
            Range.Type type = Range.Type.fromString(string);
            return new Range(type);
        }
    }

    /**
     * Parses a range, returning empty instead of throwing when the text is not a valid range.
     *
     * @param string the text to parse
     * @return the parsed range, or empty if the text is not recognised
     */
    public static Optional<Range> fromString(String string) {
        try {
            return Optional.of(Range.fromJson(string));
        } catch (IllegalArgumentException _) {
            return Optional.empty();
        }
    }

    /**
     * Writes this range back to its config representation, a number for block ranges and a name otherwise.
     *
     * @return the config value
     */
    @JsonValue
    public Object toJson() {
        if (this.type == Type.BLOCKS) {
            return this.value;
        }

        return this.type.name();
    }

    /**
     * Whether this range uses the given form.
     *
     * @param type the form to test
     * @return true if the types match
     */
    public boolean is(Type type) {
        return this.type == type;
    }

    /**
     * The named scopes a range can take. Every scope except {@code BLOCKS} carries a fixed marker value.
     */
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

        /**
         * Maps a marker value back to its scope, falling back to {@link #BLOCKS} for a plain radius.
         *
         * @param integer the raw value
         * @return the matching scope
         */
        public static Type fromInt(Integer integer) {
            return Arrays.stream(Type.values())
                    .filter(enumType -> enumType.value == integer)
                    .findAny()
                    .orElse(Type.BLOCKS);
        }

        /**
         * Maps a scope name back to its constant, ignoring case.
         *
         * @param string the scope name
         * @return the matching scope
         * @throws IllegalArgumentException if the name is not a known scope
         */
        public static Type fromString(String string) {
            return Arrays.stream(Type.values())
                    .filter(enumType -> enumType != Type.BLOCKS)
                    .filter(enumType -> enumType.name().equalsIgnoreCase(string))
                    .findAny()
                    .orElseThrow(() -> new IllegalArgumentException("Unknown range type: " + string));
        }
    }
}