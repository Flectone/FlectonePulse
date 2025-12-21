package net.flectone.pulse.module.integration.floodgate;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.util.constant.PlatformType;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FloodgateModule extends AbstractModule {

    private final FileFacade fileFacade;
    private final FloodgateIntegration floodgateIntegration;
    private final PlatformServerAdapter platformServerAdapter;

    @Override
    public void onEnable() {
        super.onEnable();

        if (platformServerAdapter.getPlatformType() == PlatformType.FABRIC) {
            // delay for init
            floodgateIntegration.hookLater();
        } else {
            floodgateIntegration.hook();
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        floodgateIntegration.unhook();
    }

    @Override
    public Integration.Floodgate config() {
        return fileFacade.integration().floodgate();
    }

    @Override
    public Permission.Integration.Floodgate permission() {
        return fileFacade.permission().integration().floodgate();
    }

    public boolean isBedrockPlayer(FEntity fPlayer) {
        if (isModuleDisabledFor(fPlayer)) return false;

        return floodgateIntegration.isBedrockPlayer(fPlayer);
    }

}
