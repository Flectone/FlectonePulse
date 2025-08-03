package net.flectone.pulse.module.message.update.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.player.PlayerJoinEvent;
import net.flectone.pulse.module.message.update.UpdateModule;

@Singleton
public class UpdatePulseListener implements PulseListener {

    private final UpdateModule updateModule;

    @Inject
    public UpdatePulseListener(UpdateModule updateModule) {
        this.updateModule = updateModule;
    }

    @Pulse
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        FPlayer fPlayer = event.getPlayer();
        updateModule.send(fPlayer);
    }

}
