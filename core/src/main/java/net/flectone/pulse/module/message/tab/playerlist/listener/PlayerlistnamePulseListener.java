package net.flectone.pulse.module.message.tab.playerlist.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.player.PlayerLoadEvent;
import net.flectone.pulse.module.message.tab.playerlist.PlayerlistnameModule;

@Singleton
public class PlayerlistnamePulseListener implements PulseListener {

    private final PlayerlistnameModule playerlistnameModule;

    @Inject
    public PlayerlistnamePulseListener(PlayerlistnameModule playerlistnameModule) {
        this.playerlistnameModule = playerlistnameModule;
    }

    @Pulse
    public void onPlayerLoadEvent(PlayerLoadEvent event) {
        playerlistnameModule.update();
    }

}
