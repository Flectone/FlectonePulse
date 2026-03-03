package net.flectone.pulse.module.integration.triton;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;
import org.jspecify.annotations.Nullable;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TritonModule implements ModuleSimple {

    private final FileFacade fileFacade;
    private final TritonIntegration tritonIntegration;
    private final ListenerRegistry listenerRegistry;
    private final ModuleController moduleController;

    @Override
    public void onEnable() {
        listenerRegistry.register(TritonIntegration.class);

        tritonIntegration.hook();
    }

    @Override
    public void onDisable() {
        tritonIntegration.unhook();
    }

    @Override
    public ModuleName name() {
        return ModuleName.INTEGRATION_TRITON;
    }

    @Override
    public Integration.Triton config() {
        return fileFacade.integration().triton();
    }

    @Override
    public Permission.Integration.Triton permission() {
        return fileFacade.permission().integration().triton();
    }

    public @Nullable String getLocale(FPlayer fPlayer) {
        if (moduleController.isDisabledFor(this, fPlayer)) return null;

        return tritonIntegration.getLocale(fPlayer);
    }
}
