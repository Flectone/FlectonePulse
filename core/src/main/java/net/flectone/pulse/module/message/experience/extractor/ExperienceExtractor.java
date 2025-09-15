package net.flectone.pulse.module.message.experience.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.message.experience.model.Experience;
import net.flectone.pulse.processing.extractor.Extractor;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
public class ExperienceExtractor extends Extractor {

    @Inject
    public ExperienceExtractor() {
    }

    public Optional<Experience> extract(MinecraftTranslationKey translationKey, TranslatableComponent translatableComponent) {
        return switch (translationKey) {
            // %s has %s experience levels
            // %s has %s experience points
            case COMMANDS_EXPERIENCE_QUERY_LEVELS, COMMANDS_EXPERIENCE_QUERY_POINTS -> {
                Optional<FEntity> target = extractFEntity(translatableComponent, 0);
                if (target.isEmpty()) yield Optional.empty();

                Optional<String> amount = extractTextContent(translatableComponent, 1);
                if (amount.isEmpty()) yield Optional.empty();

                Experience experience = Experience.builder()
                        .target(target.get())
                        .amount(amount.get())
                        .build();

                yield Optional.of(experience);
            }
            // Gave %s experience levels to %s players
            // Gave %s experience points to %s players
            // Set %s experience levels on %s players
            // Set %s experience points on %s players
            case COMMANDS_EXPERIENCE_ADD_LEVELS_SUCCESS_MULTIPLE, COMMANDS_EXPERIENCE_ADD_POINTS_SUCCESS_MULTIPLE,
                 COMMANDS_EXPERIENCE_SET_LEVELS_SUCCESS_MULTIPLE, COMMANDS_EXPERIENCE_SET_POINTS_SUCCESS_MULTIPLE -> {
                Optional<String> amount = extractTextContent(translatableComponent, 0);
                if (amount.isEmpty()) yield Optional.empty();

                Optional<String> players = extractTextContent(translatableComponent, 1);
                if (players.isEmpty()) yield Optional.empty();

                Experience experience = Experience.builder()
                        .players(players.get())
                        .amount(amount.get())
                        .build();

                yield Optional.of(experience);
            }
            // Gave %s experience levels to %s
            // Gave %s experience points to %s
            // Set %s experience levels on %s
            // Set %s experience points on %s
            case COMMANDS_EXPERIENCE_ADD_LEVELS_SUCCESS_SINGLE, COMMANDS_EXPERIENCE_ADD_POINTS_SUCCESS_SINGLE,
                 COMMANDS_EXPERIENCE_SET_LEVELS_SUCCESS_SINGLE, COMMANDS_EXPERIENCE_SET_POINTS_SUCCESS_SINGLE -> {
                Optional<String> amount = extractTextContent(translatableComponent, 0);
                if (amount.isEmpty()) yield Optional.empty();

                Optional<FEntity> target = extractFEntity(translatableComponent, 1);
                if (target.isEmpty()) yield Optional.empty();

                Experience experience = Experience.builder()
                        .target(target.get())
                        .amount(amount.get())
                        .build();

                yield Optional.of(experience);
            }
            default -> Optional.empty();
        };
    }
}