package net.flectone.pulse.module.message.scoreboard;

import lombok.Getter;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Ticker;
import net.flectone.pulse.module.AbstractModuleListMessage;
import net.flectone.pulse.scheduler.TaskScheduler;

public abstract class ScoreboardModule extends AbstractModuleListMessage<Localization.Message.Scoreboard> {

    @Getter private final Message.Scoreboard message;
    private final Permission.Message.Scoreboard permission;

    private final TaskScheduler taskScheduler;

    public ScoreboardModule(FileManager fileManager,
                            TaskScheduler taskScheduler) {
        super(localization -> localization.getMessage().getScoreboard());

        this.taskScheduler = taskScheduler;

        message = fileManager.getMessage().getScoreboard();
        permission = fileManager.getPermission().getMessage().getScoreboard();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        Ticker ticker = message.getTicker();
        if (ticker.isEnable()) {
            taskScheduler.runAsyncTicker(this::send, ticker.getPeriod());
        }
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }

    public abstract void send(FPlayer fPlayer);
}
