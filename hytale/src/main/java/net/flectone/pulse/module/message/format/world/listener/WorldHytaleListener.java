package net.flectone.pulse.module.message.format.world.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hypixel.hytale.server.core.event.events.player.AddPlayerToWorldEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.listener.HytaleListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.message.format.world.WorldModule;
import net.flectone.pulse.service.FPlayerService;

import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class WorldHytaleListener implements HytaleListener {

    private final WorldModule worldModule;
    private final FPlayerService fPlayerService;
    private final TaskScheduler taskScheduler;

    public void onAddPlayerToWorldEvent(AddPlayerToWorldEvent event) {
        if (!worldModule.isEnable()) return;

        PlayerRef playerRef = event.getHolder().getComponent(PlayerRef.getComponentType());
        if (playerRef == null) return;

        UUID playerUUID = playerRef.getUuid();

        taskScheduler.runAsyncLater(() -> {
            FPlayer fPlayer = fPlayerService.getFPlayer(playerUUID);

            worldModule.update(fPlayer);
        }, 60L);
    }

}
