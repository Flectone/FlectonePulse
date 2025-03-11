package net.flectone.pulse.module.integration.luckperms;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.util.logging.FLogger;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

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
        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return false;

        return luckPerms.getPlayerAdapter(Player.class).getPermissionData(player).checkPermission(permission).asBoolean();
    }

    public int getGroupWeight(FPlayer fPlayer) {
        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return 0;

        String groupName = luckPerms.getPlayerAdapter(Player.class).getMetaData(player).getPrimaryGroup();
        if (groupName == null) return 0;

        Group group = luckPerms.getGroupManager().getGroup(groupName);
        if (group == null) return 0;

        return group.getWeight().orElse(0);
    }

    public String getPrefix(FPlayer fPlayer) {
        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return null;

        return luckPerms.getPlayerAdapter(Player.class).getMetaData(player).getPrefix();
    }

    public String getSuffix(FPlayer fPlayer) {
        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return null;

        return luckPerms.getPlayerAdapter(Player.class).getMetaData(player).getSuffix();
    }

    public Set<String> getGroups() {
        if (luckPerms == null) return Collections.emptySet();

        return luckPerms.getGroupManager().getLoadedGroups().stream()
                .map(Group::getName)
                .collect(Collectors.toSet());
    }
}
