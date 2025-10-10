package net.flectone.pulse.module.message.format.scoreboard.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.player.PlayerJoinEvent;
import net.flectone.pulse.model.event.player.PlayerLoadEvent;
import net.flectone.pulse.model.event.player.PlayerQuitEvent;
import net.flectone.pulse.module.message.format.scoreboard.ScoreboardModule;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ScoreboardPulseListener implements PulseListener {

    private final ScoreboardModule scoreboardModule;

    @Pulse
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        FPlayer fPlayer = event.getPlayer();
        scoreboardModule.create(fPlayer, false);
    }

    @Pulse
    public void onPlayerLoadEvent(PlayerLoadEvent event) {
        if (!event.isReload()) return;

        FPlayer fPlayer = event.getPlayer();
        scoreboardModule.create(fPlayer, true);
    }

    @Pulse
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        FPlayer fPlayer = event.getPlayer();
        scoreboardModule.remove(fPlayer);
    }

}
