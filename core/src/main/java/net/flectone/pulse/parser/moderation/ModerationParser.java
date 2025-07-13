package net.flectone.pulse.parser.moderation;

import lombok.NonNull;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Moderation;
import net.flectone.pulse.service.ModerationService;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;

public abstract class ModerationParser implements ArgumentParser<FPlayer, String>, BlockingSuggestionProvider.Strings<FPlayer> {

    private final Moderation.Type type;
    private final ModerationService moderationService;
    private final StringParser<FPlayer> stringParser;

    protected ModerationParser(Moderation.Type type,
                            ModerationService moderationService) {
        this.type = type;
        this.moderationService = moderationService;
        this.stringParser = new StringParser<>(StringParser.StringMode.SINGLE);
    }

    @Override
    public @NonNull ArgumentParseResult<String> parse(@NonNull CommandContext<FPlayer> context, @NonNull CommandInput input) {
        return stringParser.parse(context, input);
    }

    @Override
    public @NonNull Iterable<@NonNull String> stringSuggestions(@NonNull CommandContext<FPlayer> context, @NonNull CommandInput input) {
        return moderationService.getValidNames(type);
    }
}
