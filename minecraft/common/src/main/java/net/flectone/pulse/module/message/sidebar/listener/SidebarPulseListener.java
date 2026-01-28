package net.flectone.pulse.module.message.sidebar.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.player.PlayerLoadEvent;
import net.flectone.pulse.model.event.player.PlayerQuitEvent;
import net.flectone.pulse.module.message.sidebar.MinecraftSidebarModule;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SidebarPulseListener implements PulseListener {

    private final MinecraftSidebarModule sidebarModule;

    @Pulse
    public void onPlayerLoadEvent(PlayerLoadEvent event) {
        FPlayer fPlayer = event.player();
        sidebarModule.create(fPlayer);
    }

    @Pulse
    public void onPlayerQuit(PlayerQuitEvent event) {
        FPlayer fPlayer = event.player();
        sidebarModule.remove(fPlayer);
    }

}
