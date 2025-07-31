package net.flectone.pulse.module.message.format.scoreboard.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.event.player.PlayerLoadEvent;
import net.flectone.pulse.model.event.player.PlayerQuitEvent;
import net.flectone.pulse.module.message.format.scoreboard.ScoreboardModule;

@Singleton
public class ScoreboardPulseListener implements PulseListener {

    private final ScoreboardModule scoreboardModule;

    @Inject
    public ScoreboardPulseListener(ScoreboardModule scoreboardModule) {
        this.scoreboardModule = scoreboardModule;
    }

    @Pulse
    public void onPlayerLoadEvent(PlayerLoadEvent event) {
        FPlayer fPlayer = event.getPlayer();
        scoreboardModule.create(fPlayer);
    }

    @Pulse
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        FPlayer fPlayer = event.getPlayer();
        scoreboardModule.remove(fPlayer);
    }

}
