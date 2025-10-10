package net.flectone.pulse.module.message.death.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.death.DeathModule;
import net.flectone.pulse.module.message.death.extractor.DeathExtractor;
import net.flectone.pulse.module.message.death.model.Death;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DeathPulseListener implements PulseListener {

    private final DeathModule deathModule;
    private final DeathExtractor deathExtractor;

    @Pulse
    public void onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        MinecraftTranslationKey translationKey = event.getTranslationKey();
        if (!translationKey.startsWith("death.")) return;

        Optional<Death> death = deathExtractor.extract(event.getTranslatableComponent());
        if (death.isEmpty()) return;

        event.setCancelled(true);
        deathModule.send(event.getFPlayer(), translationKey, death.get());
    }
}
