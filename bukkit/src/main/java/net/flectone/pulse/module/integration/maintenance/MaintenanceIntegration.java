package net.flectone.pulse.module.integration.maintenance;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import eu.kennytv.maintenance.api.Maintenance;
import eu.kennytv.maintenance.api.MaintenanceProvider;
import lombok.Getter;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.util.logging.FLogger;

@Singleton
public class MaintenanceIntegration implements FIntegration {

    private final FLogger fLogger;

    private Maintenance maintenance;

    @Getter private boolean hooked;

    @Inject
    public MaintenanceIntegration(FLogger fLogger) {
        this.fLogger = fLogger;
    }

    @Override
    public void hook() {
        hooked = true;
        maintenance = MaintenanceProvider.get();
        fLogger.info("✔ Maintenance hooked");
    }

    @Override
    public void unhook() {
        hooked = false;
        fLogger.info("✖ Maintenance unhooked");
    }

    public boolean isMaintenance() {
        return maintenance != null && maintenance.isMaintenance();
    }
}
