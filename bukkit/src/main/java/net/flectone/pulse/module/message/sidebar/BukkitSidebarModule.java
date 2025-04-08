package net.flectone.pulse.module.message.sidebar;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.megavex.scoreboardlibrary.api.ScoreboardLibrary;
import net.megavex.scoreboardlibrary.api.sidebar.Sidebar;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Singleton
public class BukkitSidebarModule extends SidebarModule {

    private final Map<UUID, Sidebar> sidebarMap = new HashMap<>();

    private final ScoreboardLibrary scoreboardLibrary;
    private final MessagePipeline messagePipeline;

    @Inject
    public BukkitSidebarModule(FileManager fileManager,
                               TaskScheduler taskScheduler,
                               FPlayerService fPlayerService,
                               ScoreboardLibrary scoreboardLibrary,
                               MessagePipeline messagePipeline) {
        super(fileManager, fPlayerService, taskScheduler);

        this.scoreboardLibrary = scoreboardLibrary;
        this.messagePipeline = messagePipeline;
    }

    @Async
    @Override
    public void send(FPlayer fPlayer) {
        if (checkModulePredicates(fPlayer)) return;

        Sidebar sidebar = getNewSidebar(fPlayer);
        if (sidebar == null) return;

        String format = getNextMessage(fPlayer, getMessage().isRandom());
        if (format == null) return;

        String[] formats = format.split("<br>");
        if (formats.length == 0) return;

        sidebar.title(messagePipeline.builder(fPlayer, formats[0]).build());

        for (int i = 1; i < formats.length; i++) {
            sidebar.line(i, messagePipeline.builder(fPlayer, formats[i]).build());
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


    @Override
    public List<String> getAvailableMessages(FPlayer fPlayer) {
        return joinMultiList(resolveLocalization(fPlayer).getValues());
    }
}
