package net.flectone.pulse.module.integration.triton;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import org.jetbrains.annotations.Nullable;

@Singleton
public class TritonModule extends AbstractModule {

    private final FileResolver fileResolver;
    private final TritonIntegration tritonIntegration;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public TritonModule(FileResolver fileResolver,
                        TritonIntegration tritonIntegration,
                        ListenerRegistry listenerRegistry) {
        this.fileResolver = fileResolver;
        this.tritonIntegration = tritonIntegration;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission());

        listenerRegistry.register(TritonIntegration.class);

        tritonIntegration.hook();
    }

    @Override
    public void onDisable() {
        tritonIntegration.unhook();
    }

    @Override
    public Integration.Triton config() {
        return fileResolver.getIntegration().getTriton();
    }

    @Override
    public Permission.Integration.Triton permission() {
        return fileResolver.getPermission().getIntegration().getTriton();
    }

    @Nullable
    public String getLocale(FPlayer fPlayer) {
        if (isModuleDisabledFor(fPlayer)) return null;

        return tritonIntegration.getLocale(fPlayer);
    }
}
