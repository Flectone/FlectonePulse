package net.flectone.pulse.processing.parser.player;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.file.FileFacade;

import java.util.List;

@Singleton
public class OfflinePlayerParser extends PlayerParser {

    private final FPlayerService fPlayerService;

    @Inject
    public OfflinePlayerParser(FPlayerService fPlayerService,
                               SocialService socialService,
                               FileFacade fileFacade,
                               PlatformPlayerAdapter platformPlayerAdapter,
                               PermissionChecker permissionChecker) {
        super(fPlayerService, socialService, fileFacade, platformPlayerAdapter, permissionChecker);

        this.fPlayerService = fPlayerService;
    }

    @Override
    public List<String> createSuggestions(FPlayer sender) {
        return fPlayerService.findAllFPlayers().stream()
                .filter(fPlayer -> !fPlayer.isUnknown() && !fPlayer.isConsole())
                .filter(fPlayer -> isVisible(sender, fPlayer))
                .map(FEntity::name)
                .toList();
    }

}
