package net.flectone.pulse.module.integration.minimotd;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.util.logging.FLogger;

@Singleton
public class MiniMOTDIntegration implements FIntegration {

    private final FLogger fLogger;

    @Getter
    private boolean hooked;

    @Inject
    public MiniMOTDIntegration(FLogger fLogger) {
        this.fLogger = fLogger;
    }

    @Override
    public void hook() {
        hooked = true;
        fLogger.info("MiniMOTD hooked");
    }
}
