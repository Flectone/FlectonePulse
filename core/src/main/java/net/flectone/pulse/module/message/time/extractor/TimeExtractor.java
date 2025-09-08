package net.flectone.pulse.module.message.time.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.processing.extractor.Extractor;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
public class TimeExtractor extends Extractor {

    @Inject
    public TimeExtractor() {
    }

    public Optional<String> extract(TranslatableComponent translatableComponent) {
        if (translatableComponent.arguments().isEmpty()) return Optional.empty();
        if (!(translatableComponent.arguments().getFirst().asComponent() instanceof TextComponent amountComponent)) return Optional.empty();

        return Optional.of(amountComponent.content());
    }

}
