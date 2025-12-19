package net.flectone.pulse.module.integration.litebans;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.util.ExternalModeration;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.integration.litebans.listener.LiteBansPulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class LiteBansModule extends AbstractModule {

    private final FileFacade fileFacade;
    private final LiteBansIntegration liteBansIntegration;
    private final ListenerRegistry listenerRegistry;

    @Override
    public void onEnable() {
        super.onEnable();

        liteBansIntegration.hook();

        listenerRegistry.register(LiteBansPulseListener.class);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        liteBansIntegration.unhook();
    }

    @Override
    public Integration.Litebans config() {
        return fileFacade.integration().litebans();
    }

    @Override
    public Permission.Integration.Litebans permission() {
        return fileFacade.permission().integration().litebans();
    }

    public boolean isMuted(FEntity fEntity) {
        if (isModuleDisabledFor(fEntity)) return false;

        return liteBansIntegration.isMuted(fEntity);
    }

    public ExternalModeration getMute(FEntity fEntity) {
        if (isModuleDisabledFor(fEntity)) return null;

        return liteBansIntegration.getMute(fEntity);
    }

    public boolean isHooked() {
        return liteBansIntegration.isHooked();
    }
}
