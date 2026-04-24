package net.flectone.pulse.module.message.tab.playerlist.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.player.PlayerJoinEvent;
import net.flectone.pulse.model.event.player.PlayerLoadEvent;
import net.flectone.pulse.module.message.tab.playerlist.MinecraftPlayerlistnameModule;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MinecraftPulsePlayerlistnameListener implements PulseListener {

    private final MinecraftPlayerlistnameModule playerlistnameModule;

    @Pulse
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        playerlistnameModule.update();
    }

    @Pulse
    public void onPlayerLoadEvent(PlayerLoadEvent event) {
        if (!event.reload()) return;

        playerlistnameModule.update();
    }

}
