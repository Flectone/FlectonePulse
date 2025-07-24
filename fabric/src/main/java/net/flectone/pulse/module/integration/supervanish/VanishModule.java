package net.flectone.pulse.module.integration.supervanish;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Integration;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.resolver.FileResolver;

@Singleton
public class VanishModule extends AbstractModule {

    private final Integration.Supervanish config;
    private final Permission.Integration.Supervanish permission;
    private final VanishIntegration vanishIntegration;

    @Inject
    public VanishModule(FileResolver fileResolver,
                        VanishIntegration vanishIntegration) {
        this.config = fileResolver.getIntegration().getSupervanish();
        this.permission = fileResolver.getPermission().getIntegration().getSupervanish();
        this.vanishIntegration = vanishIntegration;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        vanishIntegration.hook();
    }

    @Override
    public void onDisable() {
        vanishIntegration.unhook();
    }

    @Override
    protected boolean isConfigEnable() {
        return config.isEnable();
    }

    public boolean isVanished(FEntity sender) {
        if (checkModulePredicates(sender)) return false;

        return vanishIntegration.isVanished(sender);
    }
}
