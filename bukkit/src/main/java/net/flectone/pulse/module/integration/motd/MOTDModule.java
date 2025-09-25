package net.flectone.pulse.module.integration.motd;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.status.StatusModule;
import net.flectone.pulse.processing.resolver.FileResolver;

@Singleton
public class MOTDModule extends AbstractModule {

    private final FileResolver fileResolver;
    private final MOTDIntegration motdIntegration;
    private final StatusModule statusModule;

    @Inject
    public MOTDModule(FileResolver fileResolver,
                      MOTDIntegration motdIntegration,
                      StatusModule statusModule) {
        this.fileResolver = fileResolver;
        this.motdIntegration = motdIntegration;
        this.statusModule = statusModule;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission());

        motdIntegration.hook();

        statusModule.addPredicate(fPlayer -> config().isDisableFlectonepulseStatus() && isHooked());
    }

    @Override
    public void onDisable() {
        motdIntegration.unhook();
    }

    @Override
    public Integration.MOTD config() {
        return fileResolver.getIntegration().getMotd();
    }

    @Override
    public Permission.Integration.MOTD permission() {
        return fileResolver.getPermission().getIntegration().getMotd();
    }

    public boolean isHooked() {
        return motdIntegration.isHooked();
    }
}
