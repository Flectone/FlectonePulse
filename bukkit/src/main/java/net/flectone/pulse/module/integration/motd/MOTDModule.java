package net.flectone.pulse.module.integration.motd;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.integration.motd.listener.MOTDPulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MOTDModule extends AbstractModule {

    private final FileFacade fileFacade;
    private final MOTDIntegration motdIntegration;
    private final ListenerRegistry listenerRegistry;

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
        return fileFacade.integration().motd();
    }

    @Override
    public Permission.Integration.MOTD permission() {
        return fileFacade.permission().integration().motd();
    }

    public boolean isHooked() {
        return motdIntegration.isHooked();
    }
}
