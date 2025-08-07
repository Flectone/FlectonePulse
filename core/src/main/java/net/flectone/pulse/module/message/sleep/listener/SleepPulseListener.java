package net.flectone.pulse.module.message.sleep.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.sleep.SleepModule;
import net.flectone.pulse.module.message.sleep.extractor.SleepExtractor;
import net.flectone.pulse.module.message.sleep.model.Sleep;

import java.util.Optional;

@Singleton
public class SleepPulseListener implements PulseListener {

    private final SleepModule sleepModule;
    private final SleepExtractor sleepExtractor;

    @Inject
    public SleepPulseListener(SleepModule sleepModule,
                              SleepExtractor sleepExtractor) {
        this.sleepModule = sleepModule;
        this.sleepExtractor = sleepExtractor;
    }

    @Pulse
    public void onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        if (!event.getTranslationKey().startsWith("sleep.")) return;

        Optional<Sleep> sleep = sleepExtractor.extract(event);
        if (sleep.isEmpty()) return;

        event.setCancelled(true);
        sleepModule.send(event.getFPlayer(), event.getTranslationKey(), sleep.get());
    }

}
