package net.flectone.pulse.module.integration.minimotd;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.integration.minimotd.listener.MiniMOTDPulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MiniMOTDModule extends AbstractModule {

    private final FileResolver fileResolver;
    private final MiniMOTDIntegration miniMOTDIntegration;
    private final ListenerRegistry listenerRegistry;

    @Override
    public void onEnable() {
        super.onEnable();

        miniMOTDIntegration.hook();

        listenerRegistry.register(MiniMOTDPulseListener.class);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        miniMOTDIntegration.unhook();
    }

    @Override
    public Integration.MiniMOTD config() {
        return fileResolver.getIntegration().getMinimotd();
    }

    @Override
    public Permission.Integration.MiniMOTD permission() {
        return fileResolver.getPermission().getIntegration().getMinimotd();
    }

    public boolean isHooked() {
        return miniMOTDIntegration.isHooked();
    }
}