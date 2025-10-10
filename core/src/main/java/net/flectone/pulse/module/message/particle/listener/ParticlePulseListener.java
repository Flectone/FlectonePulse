package net.flectone.pulse.module.message.particle.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.particle.ParticleModule;
import net.flectone.pulse.module.message.particle.extractor.ParticleExtractor;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ParticlePulseListener implements PulseListener {

    private final ParticleModule particleModule;
    private final ParticleExtractor particleExtractor;

    @Pulse
    public void onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        MinecraftTranslationKey translationKey = event.getTranslationKey();
        if (translationKey != MinecraftTranslationKey.COMMANDS_PARTICLE_SUCCESS) return;

        Optional<String> particle = particleExtractor.extract(event.getTranslatableComponent());
        if (particle.isEmpty()) return;

        event.setCancelled(true);
        particleModule.send(event.getFPlayer(), translationKey, particle.get());
    }

}