package net.flectone.pulse.module.integration.motd;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.status.StatusModule;

@Singleton
public class MOTDModule extends AbstractModule {

    private final Integration.MOTD integration;
    private final Permission.Integration.MOTD permission;
    private final MOTDIntegration motdIntegration;
    private final StatusModule statusModule;

    @Inject
    public MOTDModule(FileResolver fileResolver,
                      MOTDIntegration motdIntegration,
                      StatusModule statusModule) {
        this.integration = fileResolver.getIntegration().getMotd();
        this.permission = fileResolver.getPermission().getIntegration().getMotd();
        this.motdIntegration = motdIntegration;
        this.statusModule = statusModule;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        motdIntegration.hook();

        statusModule.addPredicate(fPlayer -> integration.isDisableFlectonepulseStatus() && isHooked());
    }

    @Override
    public void onDisable() {
        motdIntegration.unhook();
    }

    @Override
    protected boolean isConfigEnable() {
        return integration.isEnable();
    }

    public boolean isHooked() {
        return motdIntegration.isHooked();
    }
}
