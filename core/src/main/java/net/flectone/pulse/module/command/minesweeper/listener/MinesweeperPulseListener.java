package net.flectone.pulse.module.command.minesweeper.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.player.PlayerQuitEvent;
import net.flectone.pulse.module.command.minesweeper.MinesweeperModule;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MinesweeperPulseListener implements PulseListener {

    private final MinesweeperModule minesweeperModule;

    @Pulse
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        FPlayer fPlayer = event.player();

        minesweeperModule.removeGame(fPlayer.uuid());
    }

}
