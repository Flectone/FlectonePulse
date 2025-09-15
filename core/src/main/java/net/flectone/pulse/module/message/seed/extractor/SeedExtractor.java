package net.flectone.pulse.module.message.seed.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.processing.extractor.Extractor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
public class SeedExtractor extends Extractor {

    @Inject
    public SeedExtractor() {
    }

    // Seed: %s
    public Optional<String> extract(TranslatableComponent translatableComponent) {
        Optional<Component> seedComponent = getValueComponent(translatableComponent, 0);
        if (seedComponent.isEmpty()) return Optional.empty();

        if (seedComponent.get() instanceof TextComponent textComponent) {
            return Optional.of(textComponent.content());
        }

        return Optional.empty();
    }

}
