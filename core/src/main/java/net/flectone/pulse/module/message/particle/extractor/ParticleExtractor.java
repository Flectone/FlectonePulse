package net.flectone.pulse.module.message.particle.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.processing.extractor.Extractor;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
public class ParticleExtractor extends Extractor {

    @Inject
    public ParticleExtractor() {
    }

    public Optional<String> extract(TranslatableComponent translatableComponent) {
        if (translatableComponent.arguments().isEmpty()) return Optional.empty();
        if (!(translatableComponent.arguments().getFirst().asComponent() instanceof TextComponent nameComponent)) return Optional.empty();

        String particle = nameComponent.content();
        return Optional.of(particle);
    }

}