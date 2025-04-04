package net.flectone.pulse.module.message.format.scoreboard;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.formatter.MessageFormatter;
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

    private final FileManager fileManager;
    private final TeamManager teamManager;
    private final MessageFormatter messageFormatter;
    private final FPlayerService fPlayerService;

    @Inject
    public BukkitScoreboardModule(FileManager fileManager,
                                  TeamManager teamManager,
                                  FPlayerService fPlayerService,
                                  MessageFormatter messageFormatter) {
        super(fileManager);

        this.fileManager = fileManager;
        this.teamManager = teamManager;
        this.fPlayerService = fPlayerService;
        this.messageFormatter = messageFormatter;

        message = fileManager.getMessage().getFormat().getScoreboard();
    }

    @Async
    @Override
    public void add(FPlayer fPlayer) {
        if (checkModulePredicates(fPlayer)) return;

        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return;

        String teamName = fPlayerService.getSortedName(fPlayer);

        ScoreboardTeam playerTeam = teamManager.createIfAbsent(teamName);
        TeamDisplay teamDisplay = playerTeam.defaultDisplay();

        if (!message.getColor().isEmpty()) {
            teamDisplay.playerColor((NamedTextColor) messageFormatter.builder(fPlayer, message.getColor()).build().color());
        }

        teamDisplay.nameTagVisibility(message.isNameVisible() ? NameTagVisibility.ALWAYS : NameTagVisibility.HIDE_FOR_OTHER_TEAMS);

        Localization.Message.Format.Name localization = fileManager.getLocalization().getMessage().getFormat().getName_();

        if (!localization.getPrefix().isEmpty()) {
            teamDisplay.prefix(messageFormatter.builder(fPlayer, localization.getPrefix()).build());
        }

        if (!localization.getSuffix().isEmpty()) {
            teamDisplay.suffix(messageFormatter.builder(fPlayer, fPlayer, localization.getSuffix()).build());
        }

        teamDisplay.addEntry(fPlayer.getName());
        teamManager.addPlayer(player);
    }

    @Async
    @Override
    public void remove(FPlayer fPlayer) {
        if (checkModulePredicates(fPlayer)) return;

        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return;

        teamManager.removePlayer(player);
    }
}
