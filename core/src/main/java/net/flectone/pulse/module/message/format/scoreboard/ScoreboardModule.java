package net.flectone.pulse.module.message.format.scoreboard;

import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Ticker;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.service.FPlayerService;

public abstract class ScoreboardModule extends AbstractModule {

    private final Message.Format.Scoreboard message;
    private final Permission.Message.Format.Scoreboard permission;

    private final FPlayerService fPlayerService;
    private final TaskScheduler taskScheduler;

    public ScoreboardModule(FileResolver fileResolver,
                            FPlayerService fPlayerService,
                            TaskScheduler taskScheduler) {
        message = fileResolver.getMessage().getFormat().getScoreboard();
        permission = fileResolver.getPermission().getMessage().getFormat().getScoreboard();

        this.fPlayerService = fPlayerService;
        this.taskScheduler = taskScheduler;
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        Ticker ticker = message.getTicker();
        if (ticker.isEnable()) {
            taskScheduler.runAsyncTimer(() -> fPlayerService.getFPlayers().forEach(this::update), ticker.getPeriod());
        }
    }

    public abstract void remove(FPlayer fPlayer);

    public abstract void update(FPlayer fPlayer);

}
