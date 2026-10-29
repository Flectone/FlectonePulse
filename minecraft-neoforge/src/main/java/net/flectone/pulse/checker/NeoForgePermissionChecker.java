package net.flectone.pulse.checker;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.NeoForgeFlectonePulse;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.platform.adapter.NeoForgePlayerAdapter;
import net.flectone.pulse.platform.adapter.NeoForgeVersionAdapter;
import net.flectone.pulse.platform.registry.NeoForgePermissionRegistry;
import net.flectone.pulse.util.LazyInstance;
import net.minecraft.server.level.ServerPlayer;

@Singleton
public class NeoForgePermissionChecker extends PermissionCheckerImpl {

    private final NeoForgeFlectonePulse neoForgeFlectonePulse;
    private final NeoForgePlayerAdapter neoForgePlayerAdapter;
    private final NeoForgeVersionAdapter neoForgeVersionAdapter;

    @Inject
    public NeoForgePermissionChecker(NeoForgeFlectonePulse neoForgeFlectonePulse,
                                     NeoForgePlayerAdapter neoForgePlayerAdapter,
                                     NeoForgeVersionAdapter neoForgeVersionAdapter,
                                     NeoForgePermissionRegistry permissionRegistry,
                                     LazyInstance<IntegrationModule> integrationModule) {
        super(neoForgePlayerAdapter, integrationModule, permissionRegistry);

        this.neoForgeFlectonePulse = neoForgeFlectonePulse;
        this.neoForgePlayerAdapter = neoForgePlayerAdapter;
        this.neoForgeVersionAdapter = neoForgeVersionAdapter;
    }

    @Override
    public boolean checkPlatform(FPlayer fPlayer, String permission, boolean defaultValue) {
        if (neoForgeFlectonePulse.getMinecraftServer() == null) return true;
        if (!defaultValue) return false;

        ServerPlayer player = neoForgePlayerAdapter.getPlayer(fPlayer.uuid());
        return player == null || neoForgeVersionAdapter.hasPermission(player);
    }

}
