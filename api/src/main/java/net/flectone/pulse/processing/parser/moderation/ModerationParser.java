package net.flectone.pulse.processing.parser.moderation;

import net.flectone.pulse.model.entity.FPlayer;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;

/**
 * Command argument that parses the name of a punished player, suggesting the ones with an active punishment.
 * @author TheFaser
 */
public interface ModerationParser extends ArgumentParser<FPlayer, String>, BlockingSuggestionProvider.Strings<FPlayer> {
}
