package net.flectone.pulse.module.integration.maintenance;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import eu.kennytv.maintenance.api.Maintenance;
import eu.kennytv.maintenance.api.MaintenanceProvider;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.logging.FLogger;
import net.flectone.pulse.module.integration.FIntegration;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BukkitMaintenanceIntegration implements FIntegration {

    @Getter private final FLogger fLogger;

    @Getter private boolean hooked;

    private Maintenance maintenance;

    @Override
    public String getIntegrationName() {
        return "Maintenance";
    }

    @Override
    public void hook() {
        try {
            hooked = true;
            maintenance = MaintenanceProvider.get();
            logHook();
        } catch (Exception e) {
            lohHookFailed(e);
        }
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
