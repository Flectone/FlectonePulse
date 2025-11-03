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
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class LibertyBansModule extends AbstractModule {

    private final FileResolver fileResolver;
    private final LibertyBansIntegration libertyBansIntegration;
    private final ListenerRegistry listenerRegistry;

    @Override
    public void onEnable() {
        super.onEnable();

        libertyBansIntegration.hook();

        listenerRegistry.register(LibertyBansPulseListener.class);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        libertyBansIntegration.unhook();
    }

    @Override
    public Integration.Libertybans config() {
        return fileResolver.getIntegration().getLibertybans();
    }

    @Override
    public Permission.Integration.Libertybans permission() {
        return fileResolver.getPermission().getIntegration().getLibertybans();
    }

    public boolean isMuted(FEntity fEntity) {
        if (isModuleDisabledFor(fEntity)) return false;

        return libertyBansIntegration.isMuted(fEntity);
    }

    public ExternalModeration getMute(FEntity fEntity) {
        if (isModuleDisabledFor(fEntity)) return null;

        return libertyBansIntegration.getMute(fEntity);
    }

    public boolean isHooked() {
        return libertyBansIntegration.isHooked();
    }
}
