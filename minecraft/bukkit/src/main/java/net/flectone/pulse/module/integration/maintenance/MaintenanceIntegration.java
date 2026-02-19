package net.flectone.pulse.module.integration.maintenance;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import eu.kennytv.maintenance.api.Maintenance;
import eu.kennytv.maintenance.api.MaintenanceProvider;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.util.logging.FLogger;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MaintenanceIntegration implements FIntegration {

    @Getter private final FLogger fLogger;

    @Getter private boolean hooked;

    private Maintenance maintenance;

    @Override
    public String getIntegrationName() {
        return "Maintenance";
    }

    @Override
    public void hook() {
        hooked = true;
        maintenance = MaintenanceProvider.get();
        logHook();
    }

    @Override
    public void unhook() {
        hooked = false;
        logUnhook();
    }

    public boolean isMaintenance() {
        return maintenance != null && maintenance.isMaintenance();
    }
}
