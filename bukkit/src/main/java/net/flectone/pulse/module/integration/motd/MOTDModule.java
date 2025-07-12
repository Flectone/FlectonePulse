package net.flectone.pulse.module.integration.motd;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Integration;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.status.StatusModule;

@Singleton
public class MOTDModule extends AbstractModule {

    private final Integration.MOTD integration;
    private final Permission.Integration.MOTD permission;

    private final MOTDIntegration motdIntegration;

    @Inject
    public MOTDModule(FileResolver fileResolver,
                      MOTDIntegration motdIntegration,
                      StatusModule statusModule) {
        integration = fileResolver.getIntegration().getMotd();
        permission = fileResolver.getPermission().getIntegration().getMotd();

        this.motdIntegration = motdIntegration;

        statusModule.addPredicate(fPlayer -> integration.isDisableFlectonepulseStatus() && isHooked());
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        motdIntegration.hook();
    @Override
    public void onDisable() {
    }

    @Override
    protected boolean isConfigEnable() {
        return integration.isEnable();
    }

    public boolean isHooked() {
        return motdIntegration.isHooked();
    }
}
