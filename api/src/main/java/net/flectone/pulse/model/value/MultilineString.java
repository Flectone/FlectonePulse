package net.flectone.pulse.model.value;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.List;

/**
 * A config value written either as a single line or as a list of lines.
 *
 * @param lines the lines, in the order they were written
 * @author TheFaser
 */
public record MultilineString(List<String> lines) {

    /**
     * What the lines are joined with by default.
     */
    public static final String SEPARATOR = "<reset><br>";

    /**
     * The empty value
     */
    public static final MultilineString EMPTY = new MultilineString(List.of());

    public MultilineString {
        lines = List.copyOf(lines);
    }

    /**
     * Wraps a single line.
     *
     * @param value the text
     * @return the value
     */
    public static MultilineString of(String value) {
        return value == null || value.isEmpty() ? EMPTY : new MultilineString(List.of(value));
    }

    /**
     * Reads a value from its config representation, accepting both a single line and a list of them.
     *
     * @param object the raw config value
     * @return the parsed value
     */
    @JsonCreator
    public static MultilineString fromJson(Object object) {
        return switch (object) {
            case null -> EMPTY;
            case Collection<?> collection -> collection.isEmpty() ? EMPTY : new MultilineString(collection.stream()
                    .map(line -> line == null ? "" : String.valueOf(line))
                    .toList()
            );
            default -> of(String.valueOf(object));
        };
    }

    /**
     * The lines joined with {@link #SEPARATOR}.
     *
     * @return the text
     */
    public String value() {
        return join(SEPARATOR);
    }

    /**
     * The lines joined with something else.
     *
     * @param separator what goes between two lines
     * @return the text
     */
    public String join(String separator) {
        return String.join(separator, this.lines);
    }

    /**
     * Writes this value back to its config representation.
     * List stays a list, single line stays a single line.
     *
     * @return the config value
     */
    @JsonValue
    public Object toJson() {
        return this.lines.size() > 1 ? this.lines : value();
    }

    @Override
    public @NonNull String toString() {
        return value();
    }

}
