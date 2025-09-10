package net.flectone.pulse.module.message.experience.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.message.experience.model.Experience;
import net.flectone.pulse.processing.extractor.Extractor;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.TranslationArgument;

import java.util.List;
import java.util.Optional;

@Singleton
public class ExperienceExtractor extends Extractor {

    @Inject
    public ExperienceExtractor() {
    }

    public Optional<Experience> extract(MinecraftTranslationKey translationKey, TranslatableComponent translatableComponent) {
        List<TranslationArgument> translationArguments = translatableComponent.arguments();
        if (translationArguments.size() < 2) return Optional.empty();

        if (translationKey == MinecraftTranslationKey.COMMANDS_EXPERIENCE_QUERY_LEVELS
                || translationKey == MinecraftTranslationKey.COMMANDS_EXPERIENCE_QUERY_POINTS) {
            Optional<FEntity> optionalFEntity = extractFEntity(translationArguments.get(0).asComponent());
            if (optionalFEntity.isEmpty()) return Optional.empty();
            if (!(translationArguments.get(1).asComponent() instanceof TextComponent amountComponent)) return Optional.empty();

            String amount = amountComponent.content();
            Experience experience = new Experience(amount, null, optionalFEntity.get());
            return Optional.of(experience);
        }

        if (!(translationArguments.get(0).asComponent() instanceof TextComponent amountComponent)) return Optional.empty();

        String amount = amountComponent.content();

        if (translationKey.toString().endsWith("multiple")) {
            if (!(translationArguments.get(1).asComponent() instanceof TextComponent countComponent)) return Optional.empty();

            String count = countComponent.content();
            Experience experience = new Experience(amount, count, null);
            return Optional.of(experience);
        }

        Optional<FEntity> optionalFEntity = extractFEntity(translationArguments.get(1).asComponent());
        if (optionalFEntity.isEmpty()) return Optional.empty();

        Experience experience = new Experience(amount, null, optionalFEntity.get());
        return Optional.of(experience);
    }
}