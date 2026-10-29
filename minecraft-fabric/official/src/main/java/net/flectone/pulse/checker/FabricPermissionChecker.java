package net.flectone.pulse.checker;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.FabricFlectonePulse;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.platform.adapter.FabricPlayerAdapter;
import net.flectone.pulse.platform.adapter.FabricVersionAdapter;
import net.flectone.pulse.platform.registry.PermissionRegistry;
import net.flectone.pulse.util.LazyInstance;
import net.minecraft.server.level.ServerPlayer;

@Singleton
public class FabricPermissionChecker extends PermissionCheckerImpl {

    private final FabricFlectonePulse fabricFlectonePulse;
    private final FabricPlayerAdapter fabricPlayerAdapter;
    private final FabricVersionAdapter fabricVersionAdapter;

    @Inject
    public FabricPermissionChecker(FabricFlectonePulse fabricFlectonePulse,
                                   FabricPlayerAdapter fabricPlayerAdapter,
                                   FabricVersionAdapter fabricVersionAdapter,
                                   PermissionRegistry permissionRegistry,
                                   LazyInstance<IntegrationModule> integrationModule) {
        super(fabricPlayerAdapter, integrationModule, permissionRegistry);

        this.fabricFlectonePulse = fabricFlectonePulse;
        this.fabricPlayerAdapter = fabricPlayerAdapter;
        this.fabricVersionAdapter = fabricVersionAdapter;
    }

    @Override
    public boolean checkPlatform(FPlayer fPlayer, String permission, boolean defaultValue) {
        if (fabricFlectonePulse.getMinecraftServer() == null) return true;
        if (!defaultValue) return false;

        ServerPlayer player = fabricPlayerAdapter.getPlayer(fPlayer.uuid());
        return player == null || fabricVersionAdapter.hasPermission(player);
    }

}
