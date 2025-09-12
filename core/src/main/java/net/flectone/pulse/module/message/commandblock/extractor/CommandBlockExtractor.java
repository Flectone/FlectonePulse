package net.flectone.pulse.module.message.commandblock.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.processing.extractor.Extractor;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
public class CommandBlockExtractor extends Extractor {

    @Inject
    public CommandBlockExtractor() {
    }

    public Optional<String> extract(TranslatableComponent translatableComponent) {
        if (translatableComponent.arguments().isEmpty()) return Optional.empty();
        if (!(translatableComponent.arguments().getFirst().asComponent() instanceof TextComponent nameComponent)) return Optional.empty();

        return Optional.of(nameComponent.content());
    }

}