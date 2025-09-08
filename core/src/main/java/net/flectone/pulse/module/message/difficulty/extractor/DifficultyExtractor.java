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

    public Optional<String> extract(TranslatableComponent translatableComponent) {
        if (translatableComponent.arguments().isEmpty()) return Optional.empty();
        if (!(translatableComponent.arguments().getFirst().asComponent() instanceof TranslatableComponent difficultyComponent)) return Optional.empty();

        return Optional.of(difficultyComponent.key());
    }

}
