package net.flectone.pulse.module.integration.minimotd;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Integration;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.status.StatusModule;

@Singleton
public class MiniMOTDModule extends AbstractModule {

    private final Integration.MiniMOTD integration;
    private final Permission.Integration.MiniMOTD permission;

    private final MiniMOTDIntegration miniMOTDIntegration;

    @Inject
    public MiniMOTDModule(FileResolver fileResolver,
                          MiniMOTDIntegration miniMOTDIntegration,
                          StatusModule statusModule) {
        integration = fileResolver.getIntegration().getMinimotd();
        permission = fileResolver.getPermission().getIntegration().getMinimotd();

        this.miniMOTDIntegration = miniMOTDIntegration;

        statusModule.addPredicate(fPlayer -> integration.isDisableFlectonepulseStatus() && isHooked());
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        miniMOTDIntegration.hook();
    @Override
    public void onDisable() {
    }

    @Override
    protected boolean isConfigEnable() {
        return integration.isEnable();
    }

    public boolean isHooked() {
        return miniMOTDIntegration.isHooked();
    }
}