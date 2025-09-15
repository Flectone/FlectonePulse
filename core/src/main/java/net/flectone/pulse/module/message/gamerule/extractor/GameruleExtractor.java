package net.flectone.pulse.module.message.gamerule.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.module.message.gamerule.model.Gamerule;
import net.flectone.pulse.processing.extractor.Extractor;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
public class GameruleExtractor extends Extractor {

    @Inject
    public GameruleExtractor() {
    }

    // Gamerule %s is currently set to: %s
    // Gamerule %s is now set to: %s
    public Optional<Gamerule> extract(TranslatableComponent translatableComponent) {
        Optional<String> name = extractTextContent(translatableComponent, 0);
        if (name.isEmpty()) return Optional.empty();

        Optional<String> value = extractTextContent(translatableComponent, 1);
        if (value.isEmpty()) return Optional.empty();

        Gamerule gamerule = new Gamerule(name.get(), value.get());
        return Optional.of(gamerule);
    }

}
