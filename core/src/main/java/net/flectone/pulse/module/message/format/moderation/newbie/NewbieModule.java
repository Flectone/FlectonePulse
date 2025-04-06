package net.flectone.pulse.module.message.format.moderation.newbie;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.ExternalModeration;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleMessage;

@Singleton
public class NewbieModule extends AbstractModuleMessage<Localization.Message.Format.Moderation.Newbie> {

    private final Message.Format.Moderation.Newbie message;
    private final Permission.Message.Format.Moderation.Newbie permission;

    private final PermissionChecker permissionChecker;
    private final PlatformPlayerAdapter platformPlayerAdapter;

    @Inject
    public NewbieModule(FileManager fileManager,
                        PermissionChecker permissionChecker,
                        PlatformPlayerAdapter platformPlayerAdapter) {
        super(localization -> localization.getMessage().getFormat().getModeration().getNewbie());
        this.permissionChecker = permissionChecker;
        this.platformPlayerAdapter = platformPlayerAdapter;

        message = fileManager.getMessage().getFormat().getModeration().getNewbie();
        permission = fileManager.getPermission().getMessage().getFormat().getModeration().getNewbie();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);
        registerPermission(permission.getBypass());
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }

    public boolean isNewBie(FPlayer fPlayer) {
        if (checkModulePredicates(fPlayer)) return false;
        if (permissionChecker.check(fPlayer, permission.getBypass())) return false;

        long firstPlayed = platformPlayerAdapter.getFirstPlayed(fPlayer);
        long timeLeft = System.currentTimeMillis() - firstPlayed;
        long timeout = message.getTimeout() * 1000L;

        return timeLeft <= timeout;
    }

    public ExternalModeration getModeration(FPlayer fPlayer) {
        if (!isNewBie(fPlayer)) return null;

        long timeout = message.getTimeout() * 1000L;
        long firstPlayed = platformPlayerAdapter.getFirstPlayed(fPlayer);

        return new ExternalModeration(fPlayer.getName(),
                FPlayer.UNKNOWN.getName(),
                resolveLocalization().getReason(),
                1,
                firstPlayed,
                firstPlayed + timeout,
                false
        );
    }
}
