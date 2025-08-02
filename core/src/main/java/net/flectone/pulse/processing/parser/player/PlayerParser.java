package net.flectone.pulse.processing.parser.player;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.NonNull;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.service.FPlayerService;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;

@Singleton
public class PlayerParser implements ArgumentParser<FPlayer, String>, BlockingSuggestionProvider.Strings<FPlayer> {

    private final FPlayerService playerService;
    private final IntegrationModule integrationModule;
    private final StringParser<FPlayer> stringParser;

    @Inject
    public PlayerParser(FPlayerService playerService,
                        IntegrationModule integrationModule) {
        this.playerService = playerService;
        this.integrationModule = integrationModule;
        this.stringParser = new StringParser<>(StringParser.StringMode.SINGLE);
    }

    @Override
    public @NonNull ArgumentParseResult<String> parse(@NonNull CommandContext<FPlayer> context, @NonNull CommandInput input) {
        return stringParser.parse(context, input);
    }

    @Override
    public @NonNull Iterable<@NonNull String> stringSuggestions(@NonNull CommandContext<FPlayer> context, @NonNull CommandInput input) {
        return playerService.findOnlineFPlayers().stream()
                .filter(player -> integrationModule.isVanishedVisible(player, context.sender()))
                .map(FEntity::getName)
                .toList();
    }
}