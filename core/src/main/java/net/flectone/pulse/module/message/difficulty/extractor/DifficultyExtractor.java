package net.flectone.pulse.module.message.difficulty.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.processing.extractor.Extractor;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
public class DifficultyExtractor extends Extractor {

    @Inject
    public DifficultyExtractor() {
    }

    // The difficulty is %s
    // The difficulty has been set to %s
    public Optional<String> extract(TranslatableComponent translatableComponent) {
        return extractTranslatableKey(translatableComponent, 0);
    }

}
