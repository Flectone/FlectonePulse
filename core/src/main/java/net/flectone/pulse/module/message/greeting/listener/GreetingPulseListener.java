package net.flectone.pulse.module.message.greeting.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.player.PlayerJoinEvent;
import net.flectone.pulse.module.message.greeting.GreetingModule;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class GreetingPulseListener implements PulseListener {

    private final GreetingModule greetingModule;

    @Pulse
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        FPlayer fPlayer = event.getPlayer();
        greetingModule.send(fPlayer);
    }

}
