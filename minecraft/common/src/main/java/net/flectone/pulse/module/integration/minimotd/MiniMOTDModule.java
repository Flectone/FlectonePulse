package net.flectone.pulse.module.integration.minimotd;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.module.integration.minimotd.listener.MiniMOTDPulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MiniMOTDModule implements ModuleSimple {

    private final FileFacade fileFacade;
    private final MiniMOTDIntegration miniMOTDIntegration;
    private final ListenerRegistry listenerRegistry;

    @Override
    public void onEnable() {
        miniMOTDIntegration.hook();

        listenerRegistry.register(MiniMOTDPulseListener.class);
    }

    @Override
    public void onDisable() {
        miniMOTDIntegration.unhook();
    }

    @Override
    public ModuleName name() {
        return ModuleName.INTEGRATION_MINIMOTD;
    }

    @Override
    public Integration.MiniMOTD config() {
        return fileFacade.integration().minimotd();
    }

    @Override
    public Permission.Integration.MiniMOTD permission() {
        return fileFacade.permission().integration().minimotd();
    }

    public boolean isHooked() {
        return miniMOTDIntegration.isHooked();
    }
}