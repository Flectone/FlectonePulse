package net.flectone.pulse.module.message.afk.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.message.afk.AfkModule;
import net.flectone.pulse.service.FPlayerService;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BukkitAfkListener implements Listener {

    private final FPlayerService fPlayerService;
    private final AfkModule afkModule;
    private final TaskScheduler taskScheduler;

    @EventHandler
    public void asyncPlayerChatEvent(AsyncPlayerChatEvent event) {
        FPlayer fPlayer = fPlayerService.getFPlayer(event.getPlayer().getUniqueId());

        taskScheduler.runAsync(() -> afkModule.removeAfk("chat", fPlayer));
    }

    @EventHandler
    public void playerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
        FPlayer fPlayer = fPlayerService.getFPlayer(event.getPlayer().getUniqueId());

        String message = StringUtils.isNotEmpty(event.getMessage())
                ? event.getMessage().split(" ")[0].substring(1)
                : "";

        taskScheduler.runAsync(() -> afkModule.removeAfk(message, fPlayer));
    }
}
