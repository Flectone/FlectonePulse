package net.flectone.pulse.module.integration.floodgate;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.util.logging.FLogger;
import org.geysermc.floodgate.api.FloodgateApi;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FloodgateIntegration implements FIntegration {

    private final FLogger fLogger;
    private final TaskScheduler taskScheduler;

    private FloodgateApi floodgateApi;

    @Override
    public void hook() {
        this.floodgateApi = FloodgateApi.getInstance();
        fLogger.info("✔ Floodgate hooked");
    }

    public void hookLater() {
        taskScheduler.runAsyncLater(this::hook);
    }

    @Override
    public void unhook() {
        fLogger.info("✖ Floodgate unhooked");
    }

    public boolean isBedrockPlayer(FEntity fPlayer) {
        if (floodgateApi == null) return false;

        return floodgateApi.isFloodgatePlayer(fPlayer.uuid());
    }

}
