package net.flectone.pulse.parser.player;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.SocialService;

import java.util.List;

@Singleton
public class OfflinePlayerParserImpl extends PlayerParserImpl implements OfflinePlayerParser {

    private final FPlayerService fPlayerService;

    @Inject
    public OfflinePlayerParserImpl(FPlayerService fPlayerService,
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
