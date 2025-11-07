package net.flectone.pulse.module.integration.geyser;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.util.logging.FLogger;
import org.geysermc.geyser.api.GeyserApi;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class GeyserIntegration implements FIntegration {

    private final FLogger fLogger;

    private GeyserApi geyserApi;

    @Override
    public void hook() {
        try {
            this.geyserApi = GeyserApi.api();
            fLogger.info("✔ Geyser hooked");
        } catch (Exception ignored) {
            fLogger.warning("Geyser hook is failed, check that Geyser is turned on and working");
        }
    }

    @Async(delay = 20)
    public void hookLater() {
        hook();
    }

    @Override
    public void unhook() {
        fLogger.info("✖ Geyser unhooked");
    }

    public boolean isBedrockPlayer(FEntity fPlayer) {
        if (geyserApi == null) return false;

        return geyserApi.isBedrockPlayer(fPlayer.getUuid());
    }
}
