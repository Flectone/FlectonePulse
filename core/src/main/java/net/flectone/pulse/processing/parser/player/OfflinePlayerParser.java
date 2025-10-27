package net.flectone.pulse.processing.parser.player;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.NonNull;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.checker.PermissionChecker;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;

@Singleton
public class OfflinePlayerParser extends PlayerParser {

    private final FPlayerService fPlayerService;

    @Inject
    public OfflinePlayerParser(FPlayerService fPlayerService,
                               IntegrationModule integrationModule,
                               FileResolver fileResolver,
                               PlatformPlayerAdapter platformPlayerAdapter,
                               PermissionChecker permissionChecker) {
        super(fPlayerService, integrationModule, fileResolver, platformPlayerAdapter, permissionChecker);

        this.fPlayerService = fPlayerService;
    }

    @Override
    public @NonNull Iterable<@NonNull String> stringSuggestions(@NonNull CommandContext<FPlayer> context, @NonNull CommandInput input) {
        return fPlayerService.findAllFPlayers().stream()
                .filter(player -> !player.isUnknown())
                .filter(fPlayer -> isVisible(context.sender(), fPlayer))
                .map(FEntity::getName)
                .toList();
    }
}
