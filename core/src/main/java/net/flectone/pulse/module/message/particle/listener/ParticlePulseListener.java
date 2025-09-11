package net.flectone.pulse.module.message.particle.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.particle.ParticleModule;
import net.flectone.pulse.module.message.particle.extractor.ParticleExtractor;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

import java.util.Optional;

@Singleton
public class ParticlePulseListener implements PulseListener {

    private final ParticleModule particleModule;
    private final ParticleExtractor particleExtractor;

    @Inject
    public ParticlePulseListener(ParticleModule particleModule,
                                 ParticleExtractor particleExtractor) {
        this.particleModule = particleModule;
        this.particleExtractor = particleExtractor;
    }

    @Pulse
    public void onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        if (event.getTranslationKey() != MinecraftTranslationKey.COMMANDS_PARTICLE_SUCCESS) return;

        Optional<String> optionalParticle = particleExtractor.extract(event.getTranslatableComponent());
        if (optionalParticle.isEmpty()) return;

        event.setCancelled(true);
        particleModule.send(event.getFPlayer(), optionalParticle.get());
    }

}