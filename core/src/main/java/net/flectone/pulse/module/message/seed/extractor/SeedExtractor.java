package net.flectone.pulse.module.message.seed.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
public class SeedExtractor {

    @Inject
    public SeedExtractor() {
    }

    public Optional<String> extract(MessageReceiveEvent event) {
        TranslatableComponent translatableComponent = event.getTranslatableComponent();
        if (translatableComponent.args().isEmpty()) return Optional.empty();

        Component firstArg = translatableComponent.args().get(0);
        return switch (firstArg) {
            // modern format with chat.square_brackets
            case TranslatableComponent chatComponent when chatComponent.key().equals("chat.square_brackets")
                    && !chatComponent.args().isEmpty()
                    && chatComponent.args().get(0) instanceof TextComponent seedComponent -> Optional.of(seedComponent.content());
            // legacy format with extra
            case TextComponent textComponent when textComponent.content().equals("[")
                    && !textComponent.children().isEmpty()
                    && textComponent.children().get(0) instanceof TextComponent seedComponent -> Optional.of(seedComponent.content());
            // legacy format
            case TextComponent textComponent when !textComponent.content().isEmpty() -> Optional.of(textComponent.content());
            default -> Optional.empty();
        };
    }

}
