package net.flectone.pulse.parser.player;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.NonNull;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.service.FPlayerService;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;

@Singleton
public class OfflinePlayerParser extends PlayerParser {
    private final FPlayerService playerService;

    @Inject
    public OfflinePlayerParser(FPlayerService playerService,
                               IntegrationModule integrationModule) {
        super(playerService, integrationModule);
        this.playerService = playerService;
    }

    @Override
    public @NonNull Iterable<@NonNull String> stringSuggestions(@NonNull CommandContext<FPlayer> context, @NonNull CommandInput input) {
        return playerService.findAllFPlayers().stream()
                .filter(player -> !player.isUnknown())
                .map(FEntity::getName)
                .toList();
    }
}
