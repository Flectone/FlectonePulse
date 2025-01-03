package net.flectone.pulse.module.message.scoreboard;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.util.ComponentUtil;
import net.megavex.scoreboardlibrary.api.ScoreboardLibrary;
import net.megavex.scoreboardlibrary.api.sidebar.Sidebar;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Singleton
public class BukkitScoreboardModule extends ScoreboardModule {

    private final Map<UUID, Sidebar> sidebarMap = new HashMap<>();

    private final ScoreboardLibrary scoreboardLibrary;
    private final ComponentUtil componentUtil;

    @Inject
    public BukkitScoreboardModule(FileManager fileManager,
                                  ScoreboardLibrary scoreboardLibrary,
                                  ComponentUtil componentUtil) {
        super(fileManager);

        this.scoreboardLibrary = scoreboardLibrary;
        this.componentUtil = componentUtil;
    }

    @Async
    @Override
    public void send(FPlayer fPlayer) {
        if (checkModulePredicates(fPlayer)) return;

        Sidebar sidebar = getNewSidebar(fPlayer);
        if (sidebar == null) return;

        List<String> values = resolveLocalization(fPlayer).getValues()
                .stream()
                .map(strings -> String.join("<br>", strings))
                .toList();

        String format = nextMessage(fPlayer, getMessage().isRandom(), values);
        if (format == null) return;

        String[] formats = format.split("<br>");
        if (formats.length == 0) return;

        sidebar.title(componentUtil.builder(fPlayer, formats[0]).build());

        for (int i = 1; i < formats.length; i++) {
            sidebar.line(i, componentUtil.builder(fPlayer, formats[i]).build());
        }
    }

    private Sidebar getNewSidebar(FPlayer fPlayer) {
        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return null;

        Sidebar sidebar = sidebarMap.get(fPlayer.getUuid());
        if (sidebar != null) {
            sidebar.close();
        }

        sidebar = scoreboardLibrary.createSidebar();
        sidebar.addPlayer(player);

        sidebarMap.put(fPlayer.getUuid(), sidebar);
        return sidebar;
    }
}
