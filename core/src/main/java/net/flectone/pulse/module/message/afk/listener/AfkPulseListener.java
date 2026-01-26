package net.flectone.pulse.module.message.afk.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.model.event.message.MessagePrepareEvent;
import net.flectone.pulse.model.event.player.PlayerJoinEvent;
import net.flectone.pulse.model.event.player.PlayerLoadEvent;
import net.flectone.pulse.model.event.player.PlayerQuitEvent;
import net.flectone.pulse.module.message.afk.AfkModule;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.SettingText;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class AfkPulseListener implements PulseListener {

    private final AfkModule afkModule;

    @Pulse(ignoreCancelled = true)
    public void onMessagePrepareEvent(MessagePrepareEvent event) {
        String messageType = event.messageType().name();

        // check only sender-based message types
        if (event.messageType() != MessageType.CHAT && !messageType.startsWith("COMMAND_")) return;

        EventMetadata<?> eventMetadata = event.eventMetadata();
        if (!(eventMetadata.sender() instanceof FPlayer fPlayer)) return;
        if (fPlayer.getSetting(SettingText.AFK_SUFFIX) == null) return;

        int commandIndex = messageType.indexOf('_');
        String action = (commandIndex == -1 ? messageType : messageType.substring(commandIndex + 1)).toLowerCase();
        afkModule.remove(action, fPlayer);
    }

    @Pulse
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        FPlayer fPlayer = event.player();
        afkModule.remove("", fPlayer);
    }

    @Pulse
    public void onPlayerLoadEvent(PlayerLoadEvent event) {
        if (!event.reload()) return;

        FPlayer fPlayer = event.player();
        afkModule.remove("", fPlayer);
    }

    @Pulse(priority = Event.Priority.LOW)
    public void onPlayerQuit(PlayerQuitEvent event) {
        FPlayer fPlayer = event.player();
        afkModule.remove("quit", fPlayer);
    }

    @Pulse(priority = Event.Priority.HIGH)
    public Event onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = afkModule.addTag(event.context());

        return event.withContext(messageContext);
    }
}
