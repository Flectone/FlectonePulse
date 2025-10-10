package net.flectone.pulse.module.message.sleep.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.sleep.SleepModule;
import net.flectone.pulse.module.message.sleep.extractor.SleepExtractor;
import net.flectone.pulse.module.message.sleep.model.Sleep;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SleepPulseListener implements PulseListener {

    private final SleepModule sleepModule;
    private final SleepExtractor sleepExtractor;

    @Pulse
    public void onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        MinecraftTranslationKey translationKey = event.getTranslationKey();
        if (!translationKey.startsWith("sleep.")) return;

        Optional<Sleep> sleep = sleepExtractor.extract(translationKey, event.getTranslatableComponent());
        if (sleep.isEmpty()) return;

        event.setCancelled(true);
        sleepModule.send(event.getFPlayer(), event.getTranslationKey(), sleep.get());
    }

}
