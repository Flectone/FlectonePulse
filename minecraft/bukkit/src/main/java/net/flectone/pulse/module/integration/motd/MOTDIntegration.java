package net.flectone.pulse.module.integration.motd;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.util.logging.FLogger;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MOTDIntegration implements FIntegration {

    @Getter private final FLogger fLogger;

    @Getter private boolean hooked;

    @Override
    public String getIntegrationName() {
        return "MOTD";
    }

    @Override
    public void hook() {
        hooked = true;
        logHook();
    }

    @Override
    public void unhook() {
        hooked = false;
        logUnhook();
    }
}
