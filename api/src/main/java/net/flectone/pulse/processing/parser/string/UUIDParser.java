package net.flectone.pulse.processing.parser.string;

import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * Reads a uuid out of text, in either the dashed or the plain form.
 * @author TheFaser
 */
public interface UUIDParser {

    /**
     * Parses a uuid.
     *
     * @param string the text
     * @return the uuid, or null if the text is not one
     */
    @Nullable UUID parse(String string);

}
