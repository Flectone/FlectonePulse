package net.flectone.pulse.module.message.afk.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
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
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.constant.SettingText;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PulseAfkListener implements PulseListener {

    private final AfkModule afkModule;

    @Pulse(ignoreCancelled = true)
    public void onMessagePrepareEvent(MessagePrepareEvent event) {
        String messageType = event.moduleName().name();

        // check only sender-based message types
        if (event.moduleName() != ModuleName.MESSAGE_CHAT && !messageType.startsWith("COMMAND_")) return;

        EventMetadata<?> eventMetadata = event.eventMetadata();
        if (!(eventMetadata.sender() instanceof FPlayer fPlayer)) return;
        if (fPlayer.getSetting(SettingText.AFK_SUFFIX) == null) return;

        int commandIndex = messageType.indexOf('_');
        String action = (commandIndex == -1 ? messageType : messageType.substring(commandIndex + 1)).toLowerCase();
        afkModule.asyncRemoveAfk(action, fPlayer);
    }

    @Pulse
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        FPlayer fPlayer = event.player();
        afkModule.asyncRemoveAfk("", fPlayer);
    }

    @Pulse
    public void onPlayerLoadEvent(PlayerLoadEvent event) {
        if (!event.reload()) return;

        FPlayer fPlayer = event.player();
        afkModule.asyncRemoveAfk("", fPlayer);
    }

    @Pulse(priority = Event.Priority.LOW)
    public PlayerQuitEvent onPlayerQuit(PlayerQuitEvent event) {
        FPlayer fPlayer = event.player();

        return event.withPlayer(afkModule.removeAfk("quit", fPlayer));
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
