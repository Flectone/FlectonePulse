package net.flectone.pulse.module.integration.supervanish;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.processing.resolver.FileResolver;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class VanishModule extends AbstractModule {

    private final FileResolver fileResolver;
    private final VanishIntegration vanishIntegration;

    @Override
    public void onEnable() {
        super.onEnable();

        vanishIntegration.hook();
    }

    @Override
    public void onDisable() {
        super.onDisable();

        vanishIntegration.unhook();
    }

    @Override
    public Integration.Supervanish config() {
        return fileResolver.getIntegration().getSupervanish();
    }

    @Override
    public Permission.Integration.Supervanish permission() {
        return fileResolver.getPermission().getIntegration().getSupervanish();
    }

    public boolean isVanished(FEntity sender) {
        if (isModuleDisabledFor(sender)) return false;

        return vanishIntegration.isVanished(sender);
    }
}
