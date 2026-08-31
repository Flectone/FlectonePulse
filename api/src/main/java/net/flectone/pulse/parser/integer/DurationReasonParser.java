package net.flectone.pulse.parser.integer;

import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.value.Pair;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;

/**
 * Command argument that reads a duration followed by a free-text reason, so `/ban x 7d spam`
 * splits into the time and the rest.
 * @author TheFaser
 */
public interface DurationReasonParser extends ArgumentParser<FPlayer, Pair<Long, String>>, BlockingSuggestionProvider.Strings<FPlayer> {

    /**
     * The time returned when the text is not a duration.
     */
    long UNKNOWN_TIME = -1L;

    /**
     * Reads a duration.
     *
     * @param value the text
     * @return the duration in milliseconds
     */
    long parseTime(String value);

}
