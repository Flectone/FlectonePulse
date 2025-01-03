package net.flectone.pulse.module.message.scoreboard.ticker;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.module.message.scoreboard.ScoreboardModule;
import net.flectone.pulse.ticker.AbstractTicker;

@Singleton
public class ScoreboardTicker extends AbstractTicker {

    @Inject
    public ScoreboardTicker(ScoreboardModule scoreboardModule) {
        super(scoreboardModule::send);
    }

}
