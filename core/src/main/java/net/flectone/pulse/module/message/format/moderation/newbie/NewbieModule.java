package net.flectone.pulse.module.message.format.moderation.newbie;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.ExternalModeration;
import net.flectone.pulse.module.ModuleLocalization;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class NewbieModule implements ModuleLocalization<Localization.Message.Format.Moderation.Newbie> {

    private final FileFacade fileFacade;
    private final PermissionChecker permissionChecker;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final ModuleController moduleController;
    private final FPlayerService fPlayerService;

    @Override
    public ImmutableSet.Builder<PermissionSetting> permissionBuilder() {
        return ModuleLocalization.super.permissionBuilder().add(permission().bypass());
    }

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE_FORMAT_MODERATION_NEWBIE;
    }

    @Override
    public Message.Format.Moderation.Newbie config() {
        return fileFacade.message().format().moderation().newbie();
    }

    @Override
    public Permission.Message.Format.Moderation.Newbie permission() {
        return fileFacade.permission().message().format().moderation().newbie();
    }

    @Override
    public Localization.Message.Format.Moderation.Newbie localization(FEntity sender) {
        return fileFacade.localization(sender).message().format().moderation().newbie();
    }

    public boolean isNewBie(FPlayer fPlayer) {
        if (moduleController.isDisabledFor(this, fPlayer)) return false;
        if (permissionChecker.check(fPlayer, permission().bypass())) return false;

        long timeToCheck = switch (config().mode()) {
            case SINCE_JOIN -> System.currentTimeMillis() - platformPlayerAdapter.getFirstPlayed(fPlayer);
            case PLAYED_TIME -> platformPlayerAdapter.getAllTimePlayed(fPlayer);
        };

        long timeout = config().timeout() * 1000L;

        return timeToCheck <= timeout;
    }

    public ExternalModeration getModeration(FPlayer fPlayer) {
        if (!isNewBie(fPlayer)) return null;

        long timeout = config().timeout() * 1000L;
        long firstPlayed = platformPlayerAdapter.getFirstPlayed(fPlayer);

        long moderationTime = switch (config().mode()) {
            case SINCE_JOIN -> firstPlayed + timeout;
            case PLAYED_TIME -> System.currentTimeMillis() + (timeout - platformPlayerAdapter.getAllTimePlayed(fPlayer));
        };

        return new ExternalModeration(fPlayer.name(),
                fPlayerService.getConsole().name(),
                localization().formatRestrict(),
                1,
                firstPlayed,
                moderationTime,
                false
        );
    }
}
