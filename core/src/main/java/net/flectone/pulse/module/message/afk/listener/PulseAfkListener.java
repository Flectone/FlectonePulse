package net.flectone.pulse.module.message.afk.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.lifecycle.DisableEvent;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.model.event.message.MessagePrepareEvent;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.event.player.PlayerJoinEvent;
import net.flectone.pulse.model.event.player.PlayerLoadEvent;
import net.flectone.pulse.model.event.player.PlayerQuitEvent;
import net.flectone.pulse.module.message.afk.AfkModule;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.constant.PlatformType;
import net.flectone.pulse.util.constant.SettingText;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PulseAfkListener implements PulseListener {

    private final AfkModule afkModule;
    private final SocialService socialService;
    private final TaskScheduler taskScheduler;
    private final PlatformServerAdapter platformServerAdapter;

    @Pulse(ignoreCancelled = true)
    public void onMessagePrepareEvent(MessagePrepareEvent event) {
        String messageType = event.moduleName().name();

        // for bukkit, we check this manually
        if (platformServerAdapter.getPlatformType() == PlatformType.BUKKIT) return;

        // check only chat and command messages
        if (event.moduleName() != ModuleName.MESSAGE_CHAT && !messageType.startsWith("COMMAND_")) return;

        // skip afk messages
        if (event.moduleName() == ModuleName.MESSAGE_AFK || event.moduleName() == ModuleName.COMMAND_AFK) return;

        EventMetadata<?> eventMetadata = event.eventMetadata();
        if (!(eventMetadata.sender() instanceof FPlayer fPlayer)) return;

        int commandIndex = messageType.indexOf('_');
        String action = (commandIndex == -1 ? messageType : messageType.substring(commandIndex + 1)).toLowerCase();

        taskScheduler.runAsync(() -> {
            if (socialService.getSetting(fPlayer, SettingText.AFK_SUFFIX) != null) {
                afkModule.removeAfk(action, fPlayer);
            }
        });
    }

    @Pulse
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        FPlayer fPlayer = event.player();

        afkModule.removeAfk("", fPlayer);
    }

    @Pulse
    public void onPlayerLoadEvent(PlayerLoadEvent event) {
        if (!event.reload()) return;

        FPlayer fPlayer = event.player();
        afkModule.removeAfk("", fPlayer);
    }

    @Pulse(priority = Event.Priority.LOW)
    public void onPlayerQuit(PlayerQuitEvent event) {
        FPlayer fPlayer = event.player();

        afkModule.removeAfk("quit", fPlayer);
    }

    @Pulse(priority = Event.Priority.LOW)
    public void disableEvent(DisableEvent event) {
        afkModule.removeAllAfkPlayers("disable");
    }

    @Pulse(priority = Event.Priority.HIGH)
    public Event onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.context();
        if (messageContext.isFlag(MessageFlag.PLAYER_MESSAGE)) return event;

        return event.withContext(afkModule.addTag(messageContext));
    }
}
