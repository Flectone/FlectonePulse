package net.flectone.pulse.module.integration.geyser;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.PlatformType;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class GeyserModule extends AbstractModule {

    private final FileResolver fileResolver;
    private final GeyserIntegration geyserIntegration;
    private final PlatformServerAdapter platformServerAdapter;

    @Override
    public void onEnable() {
        super.onEnable();

        if (platformServerAdapter.getPlatformType() == PlatformType.FABRIC) {
            // delay for init
            geyserIntegration.hookLater();
        } else {
            geyserIntegration.hook();
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        geyserIntegration.unhook();
    }

    @Override
    public Integration.Geyser config() {
        return fileResolver.getIntegration().getGeyser();
    }

    @Override
    public Permission.Integration.Geyser permission() {
        return fileResolver.getPermission().getIntegration().getGeyser();
    }

    public boolean isBedrockPlayer(FEntity fPlayer) {
        if (isModuleDisabledFor(fPlayer)) return false;

        return geyserIntegration.isBedrockPlayer(fPlayer);
    }
}
