package net.flectone.pulse.module.message.format.moderation.newbie;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.constant.SettingText;
import net.flectone.pulse.constant.TimeType;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.value.ExternalModeration;
import net.flectone.pulse.model.value.PlayTime;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.formatter.TimeFormatter;
import net.flectone.pulse.service.PlaytimeService;
import net.flectone.pulse.service.SocialService;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class NewbieModuleImpl implements NewbieModule {

    private final FileFacade fileFacade;
    private final PermissionChecker permissionChecker;
    private final ModuleController moduleController;
    private final PlaytimeService playtimeService;
    private final SocialService socialService;

    @Override
    public Set<PermissionSetting> permissions() {
        Set<PermissionSetting> permissions = new LinkedHashSet<>(NewbieModule.super.permissions());
        permissions.add(permission().bypass());
        return Collections.unmodifiableSet(permissions);
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
    public Localization.Message.Format.Moderation.Newbie localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).message().format().moderation().newbie();
    }

    @Override
    public boolean isNewBie(FPlayer fPlayer) {
        if (moduleController.isDisabledFor(this, fPlayer)) return false;
        if (permissionChecker.check(fPlayer, permission().bypass())) return false;

        PlayTime playTime = playtimeService.getPlayTime(fPlayer);
        if (playTime == null) return false;

        long timeToCheck = switch (config().mode()) {
            case SINCE_JOIN -> TimeType.FIRST.getTime(fPlayer, playTime);
            case PLAYED_TIME -> TimeType.TOTAL_DYNAMIC.getTime(fPlayer, playTime);
        };

        long timeout = config().timeout() * TimeFormatter.MULTIPLIER;

        return timeToCheck <= timeout;
    }

    @Override
    public ExternalModeration getModeration(FPlayer fPlayer) {
        if (!isNewBie(fPlayer)) return null;

        PlayTime playTime = playtimeService.getPlayTime(fPlayer);
        if (playTime == null) return null;

        long timeout = config().timeout() * TimeFormatter.MULTIPLIER;
        long firstPlayed = playTime.first();

        long moderationTime = switch (config().mode()) {
            case SINCE_JOIN -> firstPlayed + timeout;
            case PLAYED_TIME -> System.currentTimeMillis() + (timeout - TimeType.TOTAL_DYNAMIC.getTime(fPlayer, playTime));
        };

        return new ExternalModeration(fPlayer.name(),
                fileFacade.config().logger().console(),
                localization(FPlayer.UNKNOWN).formatRestrict(),
                -1,
                firstPlayed,
                moderationTime,
                false
        );
    }
}
