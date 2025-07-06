package net.flectone.pulse.module.message.objective.belowname;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Ticker;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.objective.ObjectiveMode;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.service.FPlayerService;
import net.megavex.scoreboardlibrary.api.objective.ObjectiveDisplaySlot;
import net.megavex.scoreboardlibrary.api.objective.ObjectiveManager;
import net.megavex.scoreboardlibrary.api.objective.ScoreboardObjective;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@Singleton
public class BelownameModule extends AbstractModule {

    private final Message.Objective.Belowname message;
    private final Permission.Message.Objective.Belowname permission;

    private final ObjectiveManager objectiveManager;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final FPlayerService fPlayerService;
    private final TaskScheduler taskScheduler;

    private ObjectiveMode objectiveValueType;
    private ScoreboardObjective scoreboardObjective;

    @Inject
    public BelownameModule(FileResolver fileResolver,
                           ObjectiveManager objectiveManager,
                           PlatformPlayerAdapter platformPlayerAdapter,
                           FPlayerService fPlayerService,
                           TaskScheduler taskScheduler) {
        this.objectiveManager = objectiveManager;
        this.platformPlayerAdapter = platformPlayerAdapter;
        this.fPlayerService = fPlayerService;
        this.taskScheduler = taskScheduler;

        message = fileResolver.getMessage().getObjective().getBelowname();
        permission = fileResolver.getPermission().getMessage().getObjective().getBelowname();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        objectiveValueType = message.getMode();

        scoreboardObjective = objectiveManager.create("belowNameObjective");
        objectiveManager.display(ObjectiveDisplaySlot.belowName(), scoreboardObjective);

        Ticker ticker = message.getTicker();
        if (ticker.isEnable()) {
            taskScheduler.runAsyncTimer(() -> fPlayerService.getFPlayers().forEach(this::update), ticker.getPeriod());
        }
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    public void update(FPlayer fPlayer) {
        if (checkModulePredicates(fPlayer)) return;

        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return;

        scoreboardObjective.score(fPlayer.getName(), platformPlayerAdapter.getObjectiveScore(fPlayer.getUuid(), objectiveValueType));
        objectiveManager.addPlayer(player);
    }

    public void remove(FPlayer fPlayer) {
        if (checkModulePredicates(fPlayer)) return;

        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return;

        objectiveManager.removePlayer(player);
    }
}
