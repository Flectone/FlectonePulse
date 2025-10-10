package net.flectone.pulse.module.integration.tab;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.integration.tab.listener.TABPulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TABModule extends AbstractModule {

    private final FileResolver fileResolver;
    private final TABIntegration tabIntegration;
    private final ListenerRegistry listenerRegistry;

    @Override
    public void onEnable() {
        super.onEnable();

        tabIntegration.hook();

        listenerRegistry.register(TABPulseListener.class);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        tabIntegration.unhook();
    }

    @Override
    public Integration.TAB config() {
        return fileResolver.getIntegration().getTAB();
    }

    @Override
    public Permission.Integration.TAB permission() {
        return fileResolver.getPermission().getIntegration().getTAB();
    }

    public boolean isHooked() {
        return tabIntegration.isHooked();
    }
}
