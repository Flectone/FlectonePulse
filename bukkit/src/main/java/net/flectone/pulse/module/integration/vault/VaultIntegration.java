package net.flectone.pulse.module.integration.vault;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.logger.FLogger;
import net.flectone.pulse.module.integration.FIntegration;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

@Singleton
public class VaultIntegration implements FIntegration {

    private final Plugin plugin;
    private final FLogger fLogger;

    private Chat chat;
    private Permission permission;

    @Inject
    public VaultIntegration(Plugin plugin,
                            FLogger fLogger) {
        this.plugin = plugin;
        this.fLogger = fLogger;
    }

    @Override
    public void hook() {
        var permissionProvider = plugin.getServer().getServicesManager().getRegistration(Permission.class);
        if (permissionProvider != null) {
            this.permission = permissionProvider.getProvider();
        }

        var chatProvider = plugin.getServer().getServicesManager().getRegistration(Chat.class);
        if (chatProvider != null) {
            this.chat = chatProvider.getProvider();
        }

        fLogger.info("Vault hooked");
    }

    public boolean hasPermission(FPlayer fPlayer, String permissionName) {
        if (permission == null) return false;
        if (permissionName == null) return false;

        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player != null) {
            return permission.has(player, permissionName);
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(fPlayer.getUuid());
        World world = Bukkit.getWorlds().get(0);

        return permission.playerHas(world.getName(), offlinePlayer, permissionName);
    }

    public String getSuffix(FPlayer fPlayer) {
        if (chat == null) return null;

        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return null;

        return chat.getPlayerSuffix(player);
    }

    public String getPrefix(FPlayer fPlayer) {
        if (chat == null) return null;

        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return null;

        return chat.getPlayerPrefix(player);
    }

}
