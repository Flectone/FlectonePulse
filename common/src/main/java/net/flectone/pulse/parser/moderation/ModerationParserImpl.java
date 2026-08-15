package net.flectone.pulse.parser.moderation;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.value.Moderation;
import net.flectone.pulse.service.ModerationService;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.standard.StringParser;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class ModerationParserImpl implements ModerationParser {

    private final Cache<String, List<String>> suggestionCache = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.SECONDS)
            .maximumSize(10)
            .build();

    private final Moderation.Type type;
    private final ModerationService moderationService;
    private final StringParser<FPlayer> stringParser;

    protected ModerationParserImpl(Moderation.Type type,
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

        return suggestionCache.get(cacheKey, _ -> moderationService.getValidNames(type));
    }

}
