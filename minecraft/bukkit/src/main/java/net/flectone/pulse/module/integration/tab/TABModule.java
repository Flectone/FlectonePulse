package net.flectone.pulse.module.integration.tab;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.integration.tab.listener.TABPulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TABModule extends AbstractModule {

    private final FileFacade fileFacade;
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
    public Integration.Tab config() {
        return fileFacade.integration().tab();
    }

    @Override
    public Permission.Integration.Tab permission() {
        return fileFacade.permission().integration().tab();
    }

    public boolean isHooked() {
        return tabIntegration.isHooked();
    }
}
