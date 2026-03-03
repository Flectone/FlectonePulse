package net.flectone.pulse.module.integration.libertybans;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.util.ExternalModeration;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.integration.libertybans.listener.LibertyBansPulseListener;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class LibertyBansModule implements AbstractModule {

    private final FileFacade fileFacade;
    private final LibertyBansIntegration libertyBansIntegration;
    private final ListenerRegistry listenerRegistry;
    private final ModuleController moduleController;

    @Override
    public void onEnable() {
        libertyBansIntegration.hook();

        listenerRegistry.register(LibertyBansPulseListener.class);
    }

    @Override
    public void onDisable() {
        libertyBansIntegration.unhook();
    }

    @Override
    public ModuleName name() {
        return ModuleName.INTEGRATION_LIBERTYBANS;
    }

    @Override
    public Integration.Libertybans config() {
        return fileFacade.integration().libertybans();
    }

    @Override
    public Permission.Integration.Libertybans permission() {
        return fileFacade.permission().integration().libertybans();
    }

    public boolean isMuted(FEntity fEntity) {
        if (moduleController.isDisabledFor(this, fEntity)) return false;

        return libertyBansIntegration.isMuted(fEntity);
    }

    public ExternalModeration getMute(FEntity fEntity) {
        if (moduleController.isDisabledFor(this, fEntity)) return null;

        return libertyBansIntegration.getMute(fEntity);
    }

    public boolean isHooked() {
        return libertyBansIntegration.isHooked();
    }
}
