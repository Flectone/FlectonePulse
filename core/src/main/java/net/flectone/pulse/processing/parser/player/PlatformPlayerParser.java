package net.flectone.pulse.processing.parser.player;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.file.FileFacade;

import java.util.List;

@Singleton
public class PlatformPlayerParser extends PlayerParser {

    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final IntegrationModule integrationModule;
    private final FPlayerService fPlayerService;

    @Inject
    public PlatformPlayerParser(FPlayerService fPlayerService,
                                IntegrationModule integrationModule,
                                FileFacade fileFacade,
                                PlatformPlayerAdapter platformPlayerAdapter,
                                PermissionChecker permissionChecker) {
        super(fPlayerService, integrationModule, fileFacade, platformPlayerAdapter, permissionChecker);

        this.fPlayerService = fPlayerService;
        this.integrationModule = integrationModule;
        this.platformPlayerAdapter = platformPlayerAdapter;
    }

    @Override
    public List<String> createSuggestions(FPlayer sender) {
        return platformPlayerAdapter.getOnlinePlayers().stream()
                .map(fPlayerService::getFPlayer)
                .filter(player -> integrationModule.canSeeVanished(player, sender))
                .filter(fPlayer -> isVisible(sender, fPlayer))
                .map(FEntity::name)
                .toList();
    }

}
