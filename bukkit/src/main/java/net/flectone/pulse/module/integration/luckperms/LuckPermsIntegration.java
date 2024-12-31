package net.flectone.pulse.module.integration.luckperms;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.logger.FLogger;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.integration.FIntegration;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;

import java.util.concurrent.CompletableFuture;

@Singleton
public class LuckPermsIntegration implements FIntegration {

    private final FLogger fLogger;

    private LuckPerms luckPerms;

    @Inject
    public LuckPermsIntegration(FLogger fLogger) {
        this.fLogger = fLogger;
    }

    @Override
    public void hook() {
        this.luckPerms = LuckPermsProvider.get();
        fLogger.info("LuckPerms hooked");
    }

    public boolean hasPermission(FPlayer fPlayer, String permission) {
        if (luckPerms == null) return false;

        UserManager userManager = luckPerms.getUserManager();
        CompletableFuture<User> userFuture = userManager.loadUser(fPlayer.getUuid());

        return userFuture.join().getCachedData().getPermissionData().checkPermission(permission).asBoolean();
    }

    public int getGroupWeight(FPlayer fPlayer) {
        if (luckPerms == null) return 0;

        UserManager userManager = luckPerms.getUserManager();
        User user = userManager.getUser(fPlayer.getUuid());
        if (user == null) return 0;
        String groupName = user.getPrimaryGroup();

        Group group = luckPerms.getGroupManager().getGroup(groupName);
        if (group == null) return 0;

        return group.getWeight().orElse(0);
    }

    public String getPrefix(FPlayer fPlayer) {
        User user = luckPerms.getUserManager().getUser(fPlayer.getUuid());
        if (user == null) return null;
        return user.getCachedData().getMetaData().getPrefix();
    }

    public String getSuffix(FPlayer fPlayer) {
        User user = luckPerms.getUserManager().getUser(fPlayer.getUuid());
        if (user == null) return null;
        return user.getCachedData().getMetaData().getSuffix();
    }

}
