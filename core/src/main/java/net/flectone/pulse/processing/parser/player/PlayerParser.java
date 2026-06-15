package net.flectone.pulse.processing.parser.player;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.PotionUtil;
import net.flectone.pulse.util.file.FileFacade;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PlayerParser implements ArgumentParser<FPlayer, String>, BlockingSuggestionProvider.Strings<FPlayer> {

    private final StringParser<FPlayer> stringParser = new StringParser<>(StringParser.StringMode.SINGLE);

    private final Cache<UUID, List<String>> suggestionCache = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.SECONDS)
            .maximumSize(100)
            .build();

    private final FPlayerService playerService;
    private final SocialService socialService;
    private final FileFacade fileFacade;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final PermissionChecker permissionChecker;

    @Override
    public @NonNull ArgumentParseResult<String> parse(@NonNull CommandContext<FPlayer> context, @NonNull CommandInput input) {
        return stringParser.parse(context, input);
    }

    @Override
    public @NonNull Iterable<@NonNull String> stringSuggestions(@NonNull CommandContext<FPlayer> context, @NonNull CommandInput input) {
        return getCachedSuggestion(context.sender());
    }

    public List<String> createSuggestions(FPlayer sender) {
        return playerService.getOnlineFPlayers().stream()
                .filter(player -> socialService.canSeeVanished(player, sender))
                .filter(fPlayer -> isVisible(sender, fPlayer))
                .map(FEntity::name)
                .toList();
    }

    protected List<String> getCachedSuggestion(FPlayer sender) {
        List<String> cached = suggestionCache.getIfPresent(sender.uuid());
        if (cached != null) return cached;

        List<String> suggestions = createSuggestions(sender);

        updateCache(sender, suggestions);

        return suggestions;
    }

    protected void updateCache(FPlayer sender, List<String> suggestions) {
        suggestionCache.put(sender.uuid(), suggestions);
    }

    protected boolean isVisible(FPlayer sender, FPlayer fPlayer) {
        if (fileFacade.command().suggestInvisiblePlayers()) return true;
        if (!platformPlayerAdapter.hasPotionEffect(fPlayer, PotionUtil.INVISIBILITY_POTION_NAME)) return true;

        return permissionChecker.check(sender, fileFacade.permission().command().seeInvisiblePlayersInSuggest());
    }
}