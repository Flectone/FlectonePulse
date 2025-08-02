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

    private final Integration.Triton config;
    private final Permission.Integration.Triton permission;
    private final TritonIntegration tritonIntegration;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public TritonModule(FileResolver fileResolver,
                        TritonIntegration tritonIntegration,
                        ListenerRegistry listenerRegistry) {
        this.config = fileResolver.getIntegration().getTriton();
        this.permission = fileResolver.getPermission().getIntegration().getTriton();
        this.tritonIntegration = tritonIntegration;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        listenerRegistry.register(TritonIntegration.class);

        tritonIntegration.hook();
    }

    @Override
    public void onDisable() {
        tritonIntegration.unhook();
    }

    @Override
    protected boolean isConfigEnable() {
        return config.isEnable();
    }

    @Nullable
    public String getLocale(FPlayer fPlayer) {
        if (checkModulePredicates(fPlayer)) return null;

        return tritonIntegration.getLocale(fPlayer);
    }
}
