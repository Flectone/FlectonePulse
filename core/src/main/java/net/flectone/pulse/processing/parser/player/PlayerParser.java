package net.flectone.pulse.processing.parser.player;

import com.github.retrooper.packetevents.protocol.potion.PotionTypes;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.checker.PermissionChecker;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PlayerParser implements ArgumentParser<FPlayer, String>, BlockingSuggestionProvider.Strings<FPlayer> {

    private final StringParser<FPlayer> stringParser = new StringParser<>(StringParser.StringMode.SINGLE);

    private final FPlayerService playerService;
    private final IntegrationModule integrationModule;
    private final FileResolver fileResolver;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final PermissionChecker permissionChecker;

    @Override
    public @NonNull ArgumentParseResult<String> parse(@NonNull CommandContext<FPlayer> context, @NonNull CommandInput input) {
        return stringParser.parse(context, input);
    }

    @Override
    public @NonNull Iterable<@NonNull String> stringSuggestions(@NonNull CommandContext<FPlayer> context, @NonNull CommandInput input) {
        return playerService.findOnlineFPlayers().stream()
                .filter(player -> integrationModule.canSeeVanished(player, context.sender()))
                .filter(fPlayer -> isVisible(context.sender(), fPlayer))
                .map(FEntity::getName)
                .toList();
    }

    protected boolean isVisible(FPlayer sender, FPlayer fPlayer) {
        if (fileResolver.getCommand().isSuggestInvisiblePlayers()) return true;
        if (!platformPlayerAdapter.hasPotionEffect(fPlayer, PotionTypes.INVISIBILITY)) return true;

        return permissionChecker.check(sender, fileResolver.getPermission().getCommand().getSeeInvisiblePlayersInSuggest());
    }
}