package net.flectone.pulse.module.message.seed.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.seed.SeedModule;
import net.flectone.pulse.module.message.seed.extractor.SeedExtractor;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

import java.util.Optional;

@Singleton
public class SeedPulseListener implements PulseListener {

    private final SeedModule seedModule;
    private final SeedExtractor seedExtractor;

    @Inject
    public SeedPulseListener(SeedModule seedModule,
                             SeedExtractor seedExtractor) {
        this.seedModule = seedModule;
        this.seedExtractor = seedExtractor;
    }

    @Pulse
    public void onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        if (event.getTranslationKey() != MinecraftTranslationKey.COMMANDS_SEED_SUCCESS) return;

        Optional<String> seed = seedExtractor.extract(event);
        if (seed.isEmpty()) return;

        event.setCancelled(true);
        seedModule.send(event.getFPlayer(), seed.get());
    }

}
