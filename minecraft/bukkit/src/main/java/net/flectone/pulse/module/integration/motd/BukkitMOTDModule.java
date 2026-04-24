package net.flectone.pulse.module.integration.motd;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.module.integration.motd.listener.BukkitPulseMOTDListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BukkitMOTDModule implements ModuleSimple {

    private final FileFacade fileFacade;
    private final BukkitMOTDIntegration motdIntegration;
    private final ListenerRegistry listenerRegistry;

    @Override
    public void onEnable() {
        motdIntegration.hook();

        listenerRegistry.register(BukkitPulseMOTDListener.class);
    }

    @Override
    public void onDisable() {
        motdIntegration.unhook();
    }

    @Override
    public ModuleName name() {
        return ModuleName.INTEGRATION_MOTD;
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
