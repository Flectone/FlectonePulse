package net.flectone.pulse.util.tag;

import net.kyori.adventure.text.minimessage.ParsingException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Parsing error raised by a tag handler that failed with a non-parsing exception.
 *
 * @author TheFaser
 */
final class TagParsingException extends ParsingException {

    TagParsingException(@NonNull String message, @NonNull Throwable cause) {
        super(message, cause);
    }

    @Override
    public @NonNull String originalText() {
        return "";
    }

    @Override
    public @Nullable String detailMessage() {
        return getMessage();
    }

    @Override
    public int startIndex() {
        return LOCATION_UNKNOWN;
    }

    @Override
    public int endIndex() {
        return LOCATION_UNKNOWN;
    }

}
