package net.flectone.pulse.module.message.tab.header.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.player.PlayerJoinEvent;
import net.flectone.pulse.model.event.player.PlayerLoadEvent;
import net.flectone.pulse.module.message.tab.header.HeaderModule;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class HeaderPulseListener implements PulseListener {

    private final HeaderModule headerModule;

    @Pulse
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        FPlayer fPlayer = event.getPlayer();
        headerModule.send(fPlayer);
    }

    @Pulse
    public void onPlayerLoadEvent(PlayerLoadEvent event) {
        if (!event.isReload()) return;

        FPlayer fPlayer = event.getPlayer();
        headerModule.send(fPlayer);
    }

}
