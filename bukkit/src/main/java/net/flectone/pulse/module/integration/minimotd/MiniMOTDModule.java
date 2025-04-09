package net.flectone.pulse.module.integration.minimotd;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Integration;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.status.StatusModule;

@Singleton
public class MiniMOTDModule extends AbstractModule {

    private final Integration.MiniMOTD integration;
    private final Permission.Integration.MiniMOTD permission;

    private final MiniMOTDIntegration miniMOTDIntegration;

    @Inject
    public MiniMOTDModule(FileManager fileManager,
                          MiniMOTDIntegration miniMOTDIntegration,
                          StatusModule statusModule) {
        integration = fileManager.getIntegration().getMinimotd();
        permission = fileManager.getPermission().getIntegration().getMinimotd();

        this.miniMOTDIntegration = miniMOTDIntegration;

        statusModule.addPredicate(fPlayer -> integration.isDisableFlectonepulseStatus() && isHooked());
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        miniMOTDIntegration.hook();
    }

    @Override
    protected boolean isConfigEnable() {
        return integration.isEnable();
    }

    public boolean isHooked() {
        return miniMOTDIntegration.isHooked();
    }
}