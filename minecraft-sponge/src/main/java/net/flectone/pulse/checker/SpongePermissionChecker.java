package net.flectone.pulse.checker;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.platform.adapter.SpongePlayerAdapter;
import net.flectone.pulse.platform.registry.SpongePermissionRegistry;
import net.flectone.pulse.util.LazyInstance;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.api.util.Tristate;

@Singleton
public class SpongePermissionChecker extends PermissionCheckerImpl {

    private final SpongePlayerAdapter spongePlayerAdapter;

    @Inject
    public SpongePermissionChecker(SpongePlayerAdapter spongePlayerAdapter,
                                   SpongePermissionRegistry permissionRegistry,
                                   LazyInstance<IntegrationModule> integrationModule) {
        super(spongePlayerAdapter, integrationModule, permissionRegistry);

        this.spongePlayerAdapter = spongePlayerAdapter;
    }

    @Override
    public boolean checkPlatform(FPlayer fPlayer, String permission, boolean defaultValue) {
        ServerPlayer player = spongePlayerAdapter.getPlayer(fPlayer.uuid());
        if (player == null) return defaultValue;

        Tristate value = spongePlayerAdapter.getSpongePlayer(player).permissionValue(permission);
        return value == Tristate.UNDEFINED ? defaultValue : value.asBoolean();
    }

}
