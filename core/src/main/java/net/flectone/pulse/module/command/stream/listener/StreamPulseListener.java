package net.flectone.pulse.module.command.stream.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.model.event.player.PlayerLoadEvent;
import net.flectone.pulse.module.command.stream.StreamModule;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.util.constant.SettingText;
import org.apache.commons.lang3.StringUtils;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class StreamPulseListener implements PulseListener {

    private final StreamModule streamModule;

    @Pulse(priority = Event.Priority.HIGH)
    public void onPlayerLoadEvent(PlayerLoadEvent event) {
        FPlayer fPlayer = event.player();

        boolean hasStreamPermission = !streamModule.isModuleDisabledFor(fPlayer);

        if (fPlayer.getSetting(SettingText.STREAM_PREFIX) != null) {
            // remove prefix for non-streamers
            if (!hasStreamPermission) {
                streamModule.setStreamPrefix(fPlayer, null);
            }

            return;
        }

        if (!hasStreamPermission) return;

        // set false prefix for streamers
        String prefixFalse = streamModule.localization().prefixFalse();
        if (StringUtils.isNotEmpty(prefixFalse)) {
            streamModule.setStreamPrefix(fPlayer, prefixFalse);
        }
    }

    @Pulse(priority = Event.Priority.HIGH)
    public Event onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = streamModule.addTag(event.context());

        return event.withContext(messageContext);
    }

}
