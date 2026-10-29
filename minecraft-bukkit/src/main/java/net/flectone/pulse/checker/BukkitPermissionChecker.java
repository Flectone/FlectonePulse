package net.flectone.pulse.checker;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.registry.PermissionRegistry;
import net.flectone.pulse.util.LazyInstance;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@Singleton
public class BukkitPermissionChecker extends PermissionCheckerImpl {

    @Inject
    public BukkitPermissionChecker(PlatformPlayerAdapter platformPlayerAdapter,
                                   LazyInstance<IntegrationModule> integrationModule,
                                   PermissionRegistry permissionRegistry) {
        super(platformPlayerAdapter, integrationModule, permissionRegistry);
    }

    @Override
    public boolean checkPlatform(FPlayer fPlayer, String permission, boolean defaultValue) {
        Player player = Bukkit.getPlayer(fPlayer.uuid());
        return player == null ? defaultValue : player.hasPermission(permission);
    }

}
