package net.flectone.pulse.module.message.scoreboard;

import com.google.inject.Inject;
import lombok.Getter;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Message;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Ticker;
import net.flectone.pulse.module.AbstractModuleListMessage;
import net.flectone.pulse.module.message.scoreboard.ticker.ScoreboardTicker;

public abstract class ScoreboardModule extends AbstractModuleListMessage<Localization.Message.Scoreboard> {

    @Getter private final Message.Scoreboard message;
    private final Permission.Message.Scoreboard permission;

    @Inject private ScoreboardTicker scoreboardTicker;

    public ScoreboardModule(FileManager fileManager) {
        super(localization -> localization.getMessage().getScoreboard());

        message = fileManager.getMessage().getScoreboard();
        permission = fileManager.getPermission().getMessage().getScoreboard();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        Ticker ticker = message.getTicker();
        if (ticker.isEnable()) {
            scoreboardTicker.runTaskTimerAsync(ticker.getPeriod(), ticker.getPeriod());
        }
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }

    public abstract void send(FPlayer fPlayer);
}
