package net.flectone.pulse.module.message.execute.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.processing.extractor.Extractor;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
public class ExecuteExtractor extends Extractor {

    @Inject
    public ExecuteExtractor() {
    }

    public Optional<String> extract(MinecraftTranslationKey translationKey, TranslatableComponent translatableComponent) {
        if (translationKey == MinecraftTranslationKey.COMMANDS_EXECUTE_CONDITIONAL_PASS_COUNT) {
            if (translatableComponent.arguments().isEmpty()) return Optional.empty();
            if (!(translatableComponent.arguments().getFirst().asComponent() instanceof TextComponent countComponent)) return Optional.empty();

            String count = countComponent.content();
            return Optional.of(count);
        }

        return Optional.empty();
    }
}