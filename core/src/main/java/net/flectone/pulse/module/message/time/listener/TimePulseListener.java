package net.flectone.pulse.module.message.time.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.time.TimeModule;
import net.flectone.pulse.module.message.time.extractor.TimeExtractor;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TimePulseListener implements PulseListener {

    private final TimeModule timeModule;
    private final TimeExtractor timeExtractor;

    @Pulse
    public void onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        if (event.getTranslationKey() != MinecraftTranslationKey.COMMANDS_TIME_QUERY
                && event.getTranslationKey() != MinecraftTranslationKey.COMMANDS_TIME_SET) return;

        Optional<String> time = timeExtractor.extract(event.getTranslatableComponent());
        if (time.isEmpty()) return;

        event.setCancelled(true);
        timeModule.send(event.getFPlayer(), event.getTranslationKey(), time.get());
    }

}
