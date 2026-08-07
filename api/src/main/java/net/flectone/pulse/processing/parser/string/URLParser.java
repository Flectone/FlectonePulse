package net.flectone.pulse.processing.parser.string;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.Optional;

/**
 * Reads a url out of text, rejecting anything malformed.
 * @author TheFaser
 */
public interface URLParser {

    /**
     * Parses a url.
     *
     * @param string the text, may be null
     * @return the url, or empty if the text is not one
     */
    @NonNull Optional<URL> parse(@Nullable String string);

}
