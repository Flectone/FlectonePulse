package net.flectone.pulse.module.integration.geyser;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.constant.PlatformType;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class GeyserModule implements AbstractModule {

    private final FileFacade fileFacade;
    private final GeyserIntegration geyserIntegration;
    private final PlatformServerAdapter platformServerAdapter;
    private final ModuleController moduleController;

    @Override
    public void onEnable() {
        if (platformServerAdapter.getPlatformType() == PlatformType.FABRIC) {
            // delay for init
            geyserIntegration.hookLater();
        } else {
            geyserIntegration.hook();
        }
    }

    @Override
    public void onDisable() {
        geyserIntegration.unhook();
    }

    @Override
    public ModuleName name() {
        return ModuleName.INTEGRATION_GEYSER;
    }

    @Override
    public Integration.Geyser config() {
        return fileFacade.integration().geyser();
    }

    @Override
    public Permission.Integration.Geyser permission() {
        return fileFacade.permission().integration().geyser();
    }

    public boolean isBedrockPlayer(FEntity fPlayer) {
        if (moduleController.isDisabledFor(this, fPlayer)) return false;

        return geyserIntegration.isBedrockPlayer(fPlayer);
    }
}
