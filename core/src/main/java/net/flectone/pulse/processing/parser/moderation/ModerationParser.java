package net.flectone.pulse.processing.parser.moderation;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Moderation;
import net.flectone.pulse.service.ModerationService;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class ModerationParser implements ArgumentParser<FPlayer, String>, BlockingSuggestionProvider.Strings<FPlayer> {

    private final Cache<String, List<String>> suggestionCache = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.SECONDS)
            .maximumSize(10)
            .build();

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
        String cacheKey = type.name() + moderationService.getServer(type);

        List<String> cached = suggestionCache.getIfPresent(cacheKey);
        if (cached != null) return cached;

        List<String> suggestions = moderationService.getValidNames(type);
        suggestionCache.put(cacheKey, suggestions);

        return suggestions;
    }

}
