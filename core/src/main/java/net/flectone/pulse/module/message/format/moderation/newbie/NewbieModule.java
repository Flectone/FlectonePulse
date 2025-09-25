package net.flectone.pulse.module.message.format.moderation.newbie;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.ExternalModeration;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.PlatformType;
import net.flectone.pulse.util.logging.FLogger;

@Singleton
public class NewbieModule extends AbstractModuleLocalization<Localization.Message.Format.Moderation.Newbie> {

    private final FileResolver fileResolver;
    private final PermissionChecker permissionChecker;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final PlatformServerAdapter platformServerAdapter;
    private final FLogger fLogger;

    @Inject
    public NewbieModule(FileResolver fileResolver,
                        PermissionChecker permissionChecker,
                        PlatformPlayerAdapter platformPlayerAdapter,
                        PlatformServerAdapter platformServerAdapter,
                        FLogger fLogger) {
        super(MessageType.NEWBIE);

        this.fileResolver = fileResolver;
        this.permissionChecker = permissionChecker;
        this.platformPlayerAdapter = platformPlayerAdapter;
        this.platformServerAdapter = platformServerAdapter;
        this.fLogger = fLogger;
    }

    @Override
    public void onEnable() {
        if (platformServerAdapter.getPlatformType() == PlatformType.FABRIC
                && config().getMode() == Message.Format.Moderation.Newbie.Mode.PLAYED_TIME) {
            fLogger.warning("Newbie module is disabled! Mode PLAYED_TIME is not supported on Fabric");
            return;
        }

        registerModulePermission(permission());
        registerPermission(permission().getBypass());
    }

    @Override
    public Message.Format.Moderation.Newbie config() {
        return fileResolver.getMessage().getFormat().getModeration().getNewbie();
    }

    @Override
    public Permission.Message.Format.Moderation.Newbie permission() {
        return fileResolver.getPermission().getMessage().getFormat().getModeration().getNewbie();
    }

    @Override
    public Localization.Message.Format.Moderation.Newbie localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getMessage().getFormat().getModeration().getNewbie();
    }

    public boolean isNewBie(FPlayer fPlayer) {
        if (isModuleDisabledFor(fPlayer)) return false;
        if (permissionChecker.check(fPlayer, permission().getBypass())) return false;

        long timeToCheck = switch (config().getMode()) {
            case SINCE_JOIN -> System.currentTimeMillis() - platformPlayerAdapter.getFirstPlayed(fPlayer);
            case PLAYED_TIME -> platformPlayerAdapter.getAllTimePlayed(fPlayer);
        };

        long timeout = config().getTimeout() * 1000L;

        return timeToCheck <= timeout;
    }

    public ExternalModeration getModeration(FPlayer fPlayer) {
        if (!isNewBie(fPlayer)) return null;

        long timeout = config().getTimeout() * 1000L;
        long firstPlayed = platformPlayerAdapter.getFirstPlayed(fPlayer);

        long moderationTime = switch (config().getMode()) {
            case SINCE_JOIN -> firstPlayed + timeout;
            case PLAYED_TIME -> System.currentTimeMillis() + (timeout - platformPlayerAdapter.getAllTimePlayed(fPlayer));
        };

        return new ExternalModeration(fPlayer.getName(),
                FPlayer.UNKNOWN.getName(),
                localization().getReason(),
                1,
                firstPlayed,
                moderationTime,
                false
        );
    }
}
