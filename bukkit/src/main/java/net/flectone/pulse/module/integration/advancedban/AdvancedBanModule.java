package net.flectone.pulse.module.integration.advancedban;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.util.ExternalModeration;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.integration.advancedban.listener.AdvancedBanPulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;

@Singleton
public class AdvancedBanModule extends AbstractModule {

    private final FileResolver fileResolver;
    private final AdvancedBanIntegration advancedBanIntegration;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public AdvancedBanModule(FileResolver fileResolver,
                             AdvancedBanIntegration advancedBanIntegration,
                             ListenerRegistry listenerRegistry) {
        this.fileResolver = fileResolver;
        this.advancedBanIntegration = advancedBanIntegration;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        advancedBanIntegration.hook();

        listenerRegistry.register(AdvancedBanPulseListener.class);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        advancedBanIntegration.unhook();
    }

    @Override
    public Integration.Advancedban config() {
        return fileResolver.getIntegration().getAdvancedban();
    }

    @Override
    public Permission.Integration.Advancedban permission() {
        return fileResolver.getPermission().getIntegration().getAdvancedban();
    }

    public boolean isMuted(FEntity fEntity) {
        if (isModuleDisabledFor(fEntity)) return false;

        return advancedBanIntegration.isMuted(fEntity);
    }

    public ExternalModeration getMute(FEntity fEntity) {
        if (isModuleDisabledFor(fEntity)) return null;

        return advancedBanIntegration.getMute(fEntity);
    }

    public boolean isHooked() {
        return advancedBanIntegration.isHooked();
    }
}
