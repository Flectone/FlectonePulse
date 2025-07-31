package net.flectone.pulse.module.message.greeting.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.event.player.PlayerJoinEvent;
import net.flectone.pulse.module.message.greeting.GreetingModule;

@Singleton
public class GreetingPulseListener implements PulseListener {

    private final GreetingModule greetingModule;

    @Inject
    public GreetingPulseListener(GreetingModule greetingModule) {
        this.greetingModule = greetingModule;
    }

    @Pulse
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        FPlayer fPlayer = event.getPlayer();
        greetingModule.send(fPlayer);
    }

}
