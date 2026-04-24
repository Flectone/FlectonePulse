package net.flectone.pulse.module.integration.supervanish;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FabricVanishModule implements ModuleSimple {

    private final FileFacade fileFacade;
    private final FabricVanishIntegration vanishIntegration;
    private final ModuleController moduleController;

    @Override
    public void onEnable() {
        vanishIntegration.hook();
    }

    @Override
    public void onDisable() {
        vanishIntegration.unhook();
    }

    @Override
    public ModuleName name() {
        return ModuleName.INTEGRATION_SUPERVANISH;
    }

    @Override
    public Integration.Supervanish config() {
        return fileFacade.integration().supervanish();
    }

    @Override
    public Permission.Integration.Supervanish permission() {
        return fileFacade.permission().integration().supervanish();
    }

    public boolean isVanished(FEntity sender) {
        if (moduleController.isDisabledFor(this, sender)) return false;

        return vanishIntegration.isVanished(sender);
    }
}
