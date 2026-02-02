package net.flectone.pulse.module.message.sidebar.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.player.PlayerLoadEvent;
import net.flectone.pulse.model.event.player.PlayerQuitEvent;
import net.flectone.pulse.module.message.sidebar.SidebarModule;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.util.constant.PlatformType;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SidebarPulseListener implements PulseListener {

    private final SidebarModule sidebarModule;
    private final TaskScheduler taskScheduler;
    private final PlatformServerAdapter platformServerAdapter;

    @Pulse
    public void onPlayerLoadEvent(PlayerLoadEvent event) {
        if (platformServerAdapter.getPlatformType() == PlatformType.HYTALE) {
            taskScheduler.runAsyncLater(() -> sidebarModule.create(event.player()), 20L);
        } else {
            sidebarModule.create(event.player());
        }
    }

    @Pulse
    public void onPlayerQuit(PlayerQuitEvent event) {
        FPlayer fPlayer = event.player();
        sidebarModule.remove(fPlayer);
    }

}
