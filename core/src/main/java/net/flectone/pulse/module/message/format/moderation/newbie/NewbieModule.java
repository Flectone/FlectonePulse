package net.flectone.pulse.module.message.format.moderation.newbie;

import com.google.common.collect.ImmutableList;
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
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.PlatformType;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class NewbieModule extends AbstractModuleLocalization<Localization.Message.Format.Moderation.Newbie> {

    private final FileFacade fileFacade;
    private final PermissionChecker permissionChecker;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final PlatformServerAdapter platformServerAdapter;
    private final FLogger fLogger;

    @Override
    public void onEnable() {
        if (platformServerAdapter.getPlatformType() == PlatformType.FABRIC
                && config().mode() == Message.Format.Moderation.Newbie.Mode.PLAYED_TIME) {
            fLogger.warning("Newbie module with Mode PLAYED_TIME is not supported on Fabric, SINCE_JOIN will be used");

            fileFacade.updateFilePack(filePack -> filePack.withMessage(
                    filePack.message().withFormat(
                            filePack.message().format().withModeration(
                                    filePack.message().format().moderation().withNewbie(
                                            filePack.message().format().moderation().newbie().withMode(
                                                    Message.Format.Moderation.Newbie.Mode.SINCE_JOIN
                                            )
                                    )
                            )
                    )
            ));
        }

        super.onEnable();
    }

    @Override
    public ImmutableList.Builder<PermissionSetting> permissionBuilder() {
        return super.permissionBuilder().add(permission().bypass());
    }

    @Override
    public MessageType messageType() {
        return MessageType.NEWBIE;
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
        if (isModuleDisabledFor(fPlayer)) return false;
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
                FPlayer.UNKNOWN_NAME,
                localization().reason(),
                1,
                firstPlayed,
                moderationTime,
                false
        );
    }
}
