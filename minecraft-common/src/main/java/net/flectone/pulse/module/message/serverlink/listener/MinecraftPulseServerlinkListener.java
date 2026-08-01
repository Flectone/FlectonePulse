package net.flectone.pulse.module.message.serverlink.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.player.PlayerJoinEvent;
import net.flectone.pulse.model.event.player.PlayerLoadEvent;
import net.flectone.pulse.module.message.serverlink.MinecraftServerlinkModule;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MinecraftPulseServerlinkListener implements PulseListener {

    private final MinecraftServerlinkModule linkModule;

    @Pulse
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        linkModule.sendLinks(event.player());
    }

    @Pulse
    public void onPlayerLoadEvent(PlayerLoadEvent event) {
        if (!event.reload()) return;

        FPlayer fPlayer = event.player();
        linkModule.sendLinks(fPlayer);
    }

}
