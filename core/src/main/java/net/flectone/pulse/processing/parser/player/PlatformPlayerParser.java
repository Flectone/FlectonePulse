package net.flectone.pulse.processing.parser.player;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.service.FPlayerService;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PlatformPlayerParser implements ArgumentParser<FPlayer, String>, BlockingSuggestionProvider.Strings<FPlayer> {

    private final StringParser<FPlayer> stringParser = new StringParser<>(StringParser.StringMode.SINGLE);

    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final FPlayerService fPlayerService;
    private final IntegrationModule integrationModule;

    @Override
    public @NonNull ArgumentParseResult<String> parse(@NonNull CommandContext<FPlayer> context, @NonNull CommandInput input) {
        return stringParser.parse(context, input);
    }

    @Override
    public @NonNull Iterable<@NonNull String> stringSuggestions(@NonNull CommandContext<FPlayer> context, @NonNull CommandInput input) {
        return platformPlayerAdapter.getOnlinePlayers().stream()
                .map(fPlayerService::getFPlayer)
                .filter(player -> integrationModule.canSeeVanished(player, context.sender()))
                .map(FEntity::getName)
                .toList();
    }
}
