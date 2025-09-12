package net.flectone.pulse.module.message.worldborder.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.module.message.worldborder.model.Worldborder;
import net.flectone.pulse.processing.extractor.Extractor;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.TranslationArgument;

import java.util.List;
import java.util.Optional;

@Singleton
public class WorldborderExtractor extends Extractor {

    @Inject
    public WorldborderExtractor() {
    }

    public Optional<Worldborder> extractValue(TranslatableComponent translatableComponent) {
        List<TranslationArgument> translationArguments = translatableComponent.arguments();
        if (translationArguments.isEmpty()) return Optional.empty();
        if (!(translationArguments.getFirst().asComponent() instanceof TextComponent valueComponent)) return Optional.empty();

        String value = valueComponent.content();
        Worldborder worldborder = new Worldborder(value, null);
        return Optional.of(worldborder);
    }

    public Optional<Worldborder> extractSecondValue(TranslatableComponent translatableComponent) {
        List<TranslationArgument> translationArguments = translatableComponent.arguments();
        if (translationArguments.size() < 2) return Optional.empty();
        if (!(translationArguments.get(0).asComponent() instanceof TextComponent valueComponent)) return Optional.empty();
        if (!(translationArguments.get(1).asComponent() instanceof TextComponent secondValueComponent)) return Optional.empty();

        String value = valueComponent.content();
        String secondValue = secondValueComponent.content();
        Worldborder worldborder = new Worldborder(value, secondValue);
        return Optional.of(worldborder);
    }

}