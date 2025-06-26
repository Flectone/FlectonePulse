package net.flectone.pulse.module.message.format.scoreboard;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.service.FPlayerService;
import net.kyori.adventure.text.format.NamedTextColor;
import net.megavex.scoreboardlibrary.api.team.ScoreboardTeam;
import net.megavex.scoreboardlibrary.api.team.TeamDisplay;
import net.megavex.scoreboardlibrary.api.team.TeamManager;
import net.megavex.scoreboardlibrary.api.team.enums.NameTagVisibility;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@Singleton
public class BukkitScoreboardModule extends ScoreboardModule {

    private final Message.Format.Scoreboard message;

    private final TeamManager teamManager;
    private final MessagePipeline messagePipeline;
    private final FPlayerService fPlayerService;

    @Inject
    public BukkitScoreboardModule(FileManager fileManager,
                                  TeamManager teamManager,
                                  FPlayerService fPlayerService,
                                  TaskScheduler taskScheduler,
                                  MessagePipeline messagePipeline) {
        super(fileManager, fPlayerService, taskScheduler);

        this.teamManager = teamManager;
        this.fPlayerService = fPlayerService;
        this.messagePipeline = messagePipeline;

        message = fileManager.getMessage().getFormat().getScoreboard();
    }

    @Override
    public void remove(FPlayer fPlayer) {
        if (checkModulePredicates(fPlayer)) return;

        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return;

        teamManager.removePlayer(player);
    }

    @Override
    public void update(FPlayer fPlayer) {
        if (checkModulePredicates(fPlayer)) return;

        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return;

        String teamName = fPlayerService.getSortedName(fPlayer);
        ScoreboardTeam playerTeam = teamManager.createIfAbsent(teamName);
        TeamDisplay teamDisplay = playerTeam.defaultDisplay();

        if (!message.getColor().isEmpty()) {
            teamDisplay.playerColor((NamedTextColor) messagePipeline.builder(fPlayer, message.getColor()).build().color());
        }

        teamDisplay.nameTagVisibility(message.isNameVisible() ? NameTagVisibility.ALWAYS : NameTagVisibility.HIDE_FOR_OTHER_TEAMS);

        if (!message.getPrefix().isEmpty()) {
            teamDisplay.prefix(messagePipeline.builder(fPlayer, message.getPrefix()).build());
        }

        if (!message.getSuffix().isEmpty()) {
            teamDisplay.suffix(messagePipeline.builder(fPlayer, message.getSuffix()).build());
        }

        teamDisplay.addEntry(fPlayer.getName());
        teamManager.addPlayer(player);
    }
}
