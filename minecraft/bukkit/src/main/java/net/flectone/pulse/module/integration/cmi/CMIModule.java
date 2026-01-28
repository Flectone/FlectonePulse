package net.flectone.pulse.module.integration.cmi;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.util.ExternalModeration;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.integration.cmi.listener.CMIPulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CMIModule extends AbstractModule {

    private final FileFacade fileFacade;
    private final CMIIntegration cmiIntegration;
    private final ListenerRegistry listenerRegistry;

    @Override
    public void onEnable() {
        super.onEnable();

        cmiIntegration.hook();

        listenerRegistry.register(CMIPulseListener.class);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        cmiIntegration.unhook();
    }

    @Override
    public Integration.CMI config() {
        return fileFacade.integration().cmi();
    }

    @Override
    public Permission.Integration.CMI permission() {
        return fileFacade.permission().integration().cmi();
    }

    public boolean isMuted(FEntity fEntity) {
        if (isModuleDisabledFor(fEntity)) return false;

        return cmiIntegration.isMuted(fEntity);
    }

    public ExternalModeration getMute(FEntity fEntity) {
        if (isModuleDisabledFor(fEntity)) return null;

        return cmiIntegration.getMute(fEntity);
    }

    public boolean isHooked() {
        return cmiIntegration.isHooked();
    }

}
