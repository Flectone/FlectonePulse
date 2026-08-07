package net.flectone.pulse.processing.parser.player;

import net.flectone.pulse.model.entity.FPlayer;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;

/**
 * Command argument that parses a player name, suggesting the players currently online.
 * @author TheFaser
 */
public interface PlayerParser extends ArgumentParser<FPlayer, String>, BlockingSuggestionProvider.Strings<FPlayer> {
}