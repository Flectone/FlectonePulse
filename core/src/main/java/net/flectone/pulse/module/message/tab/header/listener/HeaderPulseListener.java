package net.flectone.pulse.module.message.tab.header.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.player.PlayerLoadEvent;
import net.flectone.pulse.module.message.tab.header.HeaderModule;

@Singleton
public class HeaderPulseListener implements PulseListener {

    private final HeaderModule headerModule;

    @Inject
    public HeaderPulseListener(HeaderModule headerModule) {
        this.headerModule = headerModule;
    }

    @Pulse
    public void onPlayerLoadEvent(PlayerLoadEvent event) {
        FPlayer fPlayer = event.getPlayer();
        headerModule.send(fPlayer);
    }

}
