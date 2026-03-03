package net.flectone.pulse.module.integration.advancedban;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.util.ExternalModeration;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.module.integration.advancedban.listener.AdvancedBanPulseListener;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class AdvancedBanModule implements ModuleSimple {

    private final FileFacade fileFacade;
    private final AdvancedBanIntegration advancedBanIntegration;
    private final ListenerRegistry listenerRegistry;
    private final ModuleController moduleController;

    @Override
    public void onEnable() {
        advancedBanIntegration.hook();

        listenerRegistry.register(AdvancedBanPulseListener.class);
    }

    @Override
    public void onDisable() {
        advancedBanIntegration.unhook();
    }

    @Override
    public ModuleName name() {
        return ModuleName.INTEGRATION_ADVANCEDBAN;
    }

    @Override
    public Integration.Advancedban config() {
        return fileFacade.integration().advancedban();
    }

    @Override
    public Permission.Integration.Advancedban permission() {
        return fileFacade.permission().integration().advancedban();
    }

    public boolean isMuted(FEntity fEntity) {
        if (moduleController.isDisabledFor(this, fEntity)) return false;

        return advancedBanIntegration.isMuted(fEntity);
    }

    public ExternalModeration getMute(FEntity fEntity) {
        if (moduleController.isDisabledFor(this, fEntity)) return null;

        return advancedBanIntegration.getMute(fEntity);
    }

    public boolean isHooked() {
        return advancedBanIntegration.isHooked();
    }
}
