package net.flectone.pulse.module.message.objective.tabname;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Ticker;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.objective.ObjectiveMode;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.megavex.scoreboardlibrary.api.objective.ObjectiveDisplaySlot;
import net.megavex.scoreboardlibrary.api.objective.ObjectiveManager;
import net.megavex.scoreboardlibrary.api.objective.ScoreboardObjective;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@Singleton
public class TabnameModule extends AbstractModule {

    private final Message.Objective.Tabname config;
    private final Permission.Message.Objective.Tabname permission;

    private final ObjectiveManager objectiveManager;
    private final FPlayerManager fPlayerManager;
    private final TaskScheduler taskScheduler;

    private ObjectiveMode objectiveValueType;
    private ScoreboardObjective scoreboardObjective;

    @Inject
    public TabnameModule(FileManager fileManager,
                         ObjectiveManager objectiveManager,
                         FPlayerManager fPlayerManager,
                         TaskScheduler taskScheduler) {
        this.objectiveManager = objectiveManager;
        this.fPlayerManager = fPlayerManager;
        this.taskScheduler = taskScheduler;

        config = fileManager.getMessage().getObjective().getTabname();
        permission = fileManager.getPermission().getMessage().getObjective().getTabname();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        objectiveValueType = config.getMode();

        scoreboardObjective = objectiveManager.create("playerListObjective");
        objectiveManager.display(ObjectiveDisplaySlot.playerList(), scoreboardObjective);

        Ticker ticker = config.getTicker();
        if (ticker.isEnable()) {
            taskScheduler.runAsyncTicker(this::add, ticker.getPeriod());
        }
    }

    @Override
    public boolean isConfigEnable() {
        return config.isEnable();
    }

    @Async
    public void add(FPlayer fPlayer) {
        if (checkModulePredicates(fPlayer)) return;

        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return;

        scoreboardObjective.score(fPlayer.getName(), fPlayerManager.getObjectiveScore(fPlayer.getUuid(), objectiveValueType));
        objectiveManager.addPlayer(player);
    }

    @Async
    public void remove(FPlayer fPlayer) {
        if (checkModulePredicates(fPlayer)) return;

        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return;

        objectiveManager.removePlayer(player);
    }
}
