package net.flectone.pulse.module.integration.tab;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.integration.tab.listener.TABPulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;

@Singleton
public class TABModule extends AbstractModule {

    private final FileResolver fileResolver;
    private final TABIntegration tabIntegration;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public TABModule(FileResolver fileResolver,
                     TABIntegration tabIntegration,
                     ListenerRegistry listenerRegistry) {
        this.fileResolver = fileResolver;
        this.tabIntegration = tabIntegration;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission());

        tabIntegration.hook();

        listenerRegistry.register(TABPulseListener.class);
    }

    @Override
    public void onDisable() {
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
