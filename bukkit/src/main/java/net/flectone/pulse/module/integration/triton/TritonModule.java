package net.flectone.pulse.module.integration.triton;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Integration;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.registry.BukkitListenerRegistry;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.Nullable;

@Singleton
public class TritonModule extends AbstractModule {

    private final Integration.Triton config;
    private final Permission.Integration.Triton permission;

    private final TritonIntegration tritonIntegration;
    private final BukkitListenerRegistry bukkitListenerManager;

    @Inject
    public TritonModule(FileManager fileManager,
                        TritonIntegration tritonIntegration,
                        BukkitListenerRegistry bukkitListenerManager) {
        this.tritonIntegration = tritonIntegration;
        this.bukkitListenerManager = bukkitListenerManager;

        config = fileManager.getIntegration().getTriton();
        permission = fileManager.getPermission().getIntegration().getTriton();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        bukkitListenerManager.register(TritonIntegration.class, EventPriority.NORMAL);

        tritonIntegration.hook();
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
