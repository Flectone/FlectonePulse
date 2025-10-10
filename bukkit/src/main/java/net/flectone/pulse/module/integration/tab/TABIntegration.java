package net.flectone.pulse.module.integration.tab;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.util.logging.FLogger;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TABIntegration implements FIntegration {

    private final FLogger fLogger;

    @Getter private boolean hooked;

    @Override
    public void hook() {
        hooked = true;
        fLogger.info("✔ TAB hooked");
    }

    @Override
    public void unhook() {
        hooked = false;
        fLogger.info("✖ TAB unhooked");
    }
}
