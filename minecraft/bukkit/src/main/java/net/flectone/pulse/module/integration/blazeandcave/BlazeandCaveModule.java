package net.flectone.pulse.module.integration.blazeandcave;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BlazeandCaveModule implements ModuleSimple {

    private final FileFacade fileFacade;
    private final BlazeandCaveIntegration blazeandCaveIntegration;

    @Override
    public void onEnable() {
        blazeandCaveIntegration.hook();
    }

    @Override
    public void onDisable() {
        blazeandCaveIntegration.unhook();
    }

    @Override
    public ModuleName name() {
        return ModuleName.INTEGRATION_BLAZEANDCAVE;
    }

    @Override
    public Integration.Advancedban config() {
        return fileFacade.integration().advancedban();
    }

    @Override
    public Permission.Integration.Blazeandcave permission() {
        return fileFacade.permission().integration().blazeandcave();
    }

}
