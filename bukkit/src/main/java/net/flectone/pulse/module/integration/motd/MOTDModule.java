package net.flectone.pulse.module.integration.motd;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.integration.motd.listener.MOTDPulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;

@Singleton
public class MOTDModule extends AbstractModule {

    private final FileResolver fileResolver;
    private final MOTDIntegration motdIntegration;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public MOTDModule(FileResolver fileResolver,
                      MOTDIntegration motdIntegration,
                      ListenerRegistry listenerRegistry) {
        this.fileResolver = fileResolver;
        this.motdIntegration = motdIntegration;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        motdIntegration.hook();

        listenerRegistry.register(MOTDPulseListener.class);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        motdIntegration.unhook();
    }

    @Override
    public Integration.MOTD config() {
        return fileResolver.getIntegration().getMotd();
    }

    @Override
    public Permission.Integration.MOTD permission() {
        return fileResolver.getPermission().getIntegration().getMotd();
    }

    public boolean isHooked() {
        return motdIntegration.isHooked();
    }
}
