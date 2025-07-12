package net.flectone.pulse.module.integration.motd;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.util.logging.FLogger;

@Singleton
public class MOTDIntegration implements FIntegration {

    private final FLogger fLogger;

    @Getter
    private boolean hooked;

    @Inject
    public MOTDIntegration(FLogger fLogger) {
        this.fLogger = fLogger;
    }

    @Override
    public void hook() {
        hooked = true;
        fLogger.info("✔ MOTD hooked");
    }

    @Override
    public void unhook() {
        hooked = false;
        fLogger.info("✖ MOTD unhooked");
    }
}
