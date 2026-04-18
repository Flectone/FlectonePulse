package net.flectone.pulse.module.integration.blazeandcave;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.util.logging.FLogger;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BlazeandCaveIntegration implements FIntegration {

    private static boolean dispatched;

    private final PlatformServerAdapter platformServerAdapter;

    @Getter private final FLogger fLogger;
    @Getter private boolean hooked;

    @Override
    public String getIntegrationName() {
        return "BlazeandCave";
    }

    @Override
    public void hook() {
        // no need to execute commands every time reload
        if (!dispatched) {
            // I think this is the only way to do it
            platformServerAdapter.dispatchCommand("function blazeandcave:config/msg_set_vanilla_msg");
            dispatched = true;
        }

        hooked = true;
        logHook();
    }

    @Override
    public void unhook() {
        hooked = false;
        logUnhook();
    }


}
