package net.flectone.pulse.module.integration.triton;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.util.file.FileFacade;
import org.jetbrains.annotations.Nullable;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TritonModule extends AbstractModule {

    private final FileFacade fileFacade;
    private final TritonIntegration tritonIntegration;
    private final ListenerRegistry listenerRegistry;

    @Override
    public void onEnable() {
        super.onEnable();

        listenerRegistry.register(TritonIntegration.class);

        tritonIntegration.hook();
    }

    @Override
    public void onDisable() {
        super.onDisable();

        tritonIntegration.unhook();
    }

    @Override
    public Integration.Triton config() {
        return fileFacade.integration().triton();
    }

    @Override
    public Permission.Integration.Triton permission() {
        return fileFacade.permission().integration().triton();
    }

    @Nullable
    public String getLocale(FPlayer fPlayer) {
        if (isModuleDisabledFor(fPlayer)) return null;

        return tritonIntegration.getLocale(fPlayer);
    }
}
