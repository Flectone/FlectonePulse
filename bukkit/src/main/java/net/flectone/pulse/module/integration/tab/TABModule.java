package net.flectone.pulse.module.integration.tab;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.util.logging.FLogger;

@Singleton
public class TABModule extends AbstractModule {

    @Getter private final Integration.TAB integration;
    private final Permission.Integration.TAB permission;

    private final FLogger fLogger;

    @Inject
    public TABModule(FileManager fileManager,
                     FLogger fLogger) {
        integration = fileManager.getIntegration().getTAB();
        permission = fileManager.getPermission().getIntegration().getTAB();

        this.fLogger = fLogger;
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        fLogger.info("TAB hooked");
    }

    @Override
    public boolean isConfigEnable() {
        return integration.isEnable();
    }
}
