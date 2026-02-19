package net.flectone.pulse.module.integration.geyser;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.util.logging.FLogger;
import org.geysermc.geyser.api.GeyserApi;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class GeyserIntegration implements FIntegration {

    private final TaskScheduler taskScheduler;
    @Getter private final FLogger fLogger;

    private GeyserApi geyserApi;

    @Override
    public String getIntegrationName() {
        return "Geyser";
    }

    @Override
    public void hook() {
        this.geyserApi = GeyserApi.api();
        logHook();
    }

    public void hookLater() {
        taskScheduler.runAsyncLater(this::hook);
    }

    public boolean isBedrockPlayer(FEntity fPlayer) {
        if (geyserApi == null) return false;

        return geyserApi.isBedrockPlayer(fPlayer.uuid());
    }
}
