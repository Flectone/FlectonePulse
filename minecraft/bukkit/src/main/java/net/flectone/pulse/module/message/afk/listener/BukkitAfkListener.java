package net.flectone.pulse.module.message.afk.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.message.afk.AfkModule;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.constant.SettingText;
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
    private final net.flectone.pulse.module.command.afk.AfkModule afkCommandModule;
    private final ModuleController moduleController;
    private final TaskScheduler taskScheduler;
    private final SocialService socialService;

    @EventHandler
    public void asyncPlayerChatEvent(AsyncPlayerChatEvent event) {
        FPlayer fPlayer = fPlayerService.getFPlayer(event.getPlayer().getUniqueId());

        taskScheduler.runAsync(() -> {
            if (socialService.getSetting(fPlayer, SettingText.AFK_SUFFIX) != null) {
                afkModule.removeAfk("chat", fPlayer);
            }
        });
    }

    @EventHandler
    public void playerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
        FPlayer fPlayer = fPlayerService.getFPlayer(event.getPlayer().getUniqueId());

        String message = StringUtils.isNotEmpty(event.getMessage())
                ? event.getMessage().split(" ")[0].substring(1)
                : "";

        // skip afk command
        if (moduleController.isEnable(afkCommandModule) && afkCommandModule.config().aliases().stream().anyMatch(message.toLowerCase()::equals)) return;

        taskScheduler.runAsync(() -> {
            if (socialService.getSetting(fPlayer, SettingText.AFK_SUFFIX) != null) {
                afkModule.removeAfk(message, fPlayer);
            }
        });
    }
}
