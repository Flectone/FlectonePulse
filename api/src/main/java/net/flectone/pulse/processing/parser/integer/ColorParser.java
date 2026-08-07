package net.flectone.pulse.processing.parser.integer;

import net.flectone.pulse.model.entity.FPlayer;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;

/**
 * Command argument that parses a color, accepting legacy codes, named colors and hex.
 * @author TheFaser
 */
public interface ColorParser extends ArgumentParser<FPlayer, String>, BlockingSuggestionProvider.Strings<FPlayer> {
}
