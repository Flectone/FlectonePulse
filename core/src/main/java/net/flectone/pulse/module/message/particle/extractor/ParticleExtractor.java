package net.flectone.pulse.module.message.particle.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.processing.extractor.Extractor;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
public class ParticleExtractor extends Extractor {

    @Inject
    public ParticleExtractor() {
    }

    // Displaying particle %s
    public Optional<String> extract(TranslatableComponent translatableComponent) {
        return extractTextContent(translatableComponent, 0);
    }

}