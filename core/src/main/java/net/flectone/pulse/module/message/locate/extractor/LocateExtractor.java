package net.flectone.pulse.module.message.locate.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.module.message.locate.model.Locate;
import net.flectone.pulse.processing.extractor.Extractor;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.TranslationArgument;

import java.util.List;
import java.util.Optional;

@Singleton
public class LocateExtractor extends Extractor {

    @Inject
    public LocateExtractor() {
    }

    public Optional<Locate> extract(TranslatableComponent translatableComponent) {
        List<TranslationArgument> translationArguments = translatableComponent.arguments();
        if (translationArguments.size() < 3) return Optional.empty();
        if (!(translationArguments.get(0).asComponent() instanceof TextComponent nameComponent)) return Optional.empty();
        if (!(translationArguments.get(1).asComponent() instanceof TranslatableComponent chatComponent)) return Optional.empty();
        if (!(translationArguments.get(2).asComponent() instanceof TextComponent blocksComponent)) return Optional.empty();

        if (chatComponent.arguments().isEmpty()) return Optional.empty();
        if (!(chatComponent.arguments().getFirst().asComponent() instanceof TranslatableComponent coordinatesComponent)) return Optional.empty();

        List<TranslationArgument> coordinatesArguments = coordinatesComponent.arguments();
        if (coordinatesArguments.size() < 3) return Optional.empty();
        if (!(coordinatesArguments.get(0).asComponent() instanceof TextComponent xComponent)) return Optional.empty();
        if (!(coordinatesArguments.get(1).asComponent() instanceof TextComponent yComponent)) return Optional.empty();
        if (!(coordinatesArguments.get(2).asComponent() instanceof TextComponent zComponent)) return Optional.empty();

        String name = nameComponent.content();
        String x = xComponent.content();
        String y = yComponent.content();
        String z = zComponent.content();
        String blocks = blocksComponent.content();
        Locate locate = new Locate(name, x, y, z, blocks);
        return Optional.of(locate);
    }

}