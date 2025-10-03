package net.flectone.pulse.module.message.advancement.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.message.advancement.model.Advancement;
import net.flectone.pulse.processing.extractor.Extractor;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
public class AdvancementExtractor extends Extractor {

    @Inject
    public AdvancementExtractor() {
    }

    // %s has completed the challenge %s
    // %s has reached the goal %s
    // %s has made the advancement %s
    public Optional<Advancement> extractAdvancement(TranslatableComponent translatableComponent) {
        Optional<FEntity> target = extractFEntity(translatableComponent, 0);
        if (target.isEmpty()) return Optional.empty();

        Optional<Component> advancementComponent = getValueComponent(translatableComponent, 1);
        if (advancementComponent.isEmpty()) return Optional.empty();

        Advancement advancement = Advancement.builder()
                .advancementComponent(advancementComponent.get())
                .target(target.get())
                .build();

        return Optional.of(advancement);
    }

    public Optional<Advancement> extractCriterionAdvancement(MinecraftTranslationKey translationKey, TranslatableComponent translatableComponent) {
        Optional<String> criterion = extractTextContent(translatableComponent, 0);
        if (criterion.isEmpty()) return Optional.empty();

        Optional<Component> advancementComponent = getValueComponent(translatableComponent, 1);
        if (advancementComponent.isEmpty()) return Optional.empty();

        return switch (translationKey) {
            // Granted criterion '%s' of advancement %s to %s players
            // Revoked criterion '%s' of advancement %s from %s players
            case COMMANDS_ADVANCEMENT_GRANT_CRITERION_TO_MANY_SUCCESS, COMMANDS_ADVANCEMENT_REVOKE_CRITERION_TO_MANY_SUCCESS -> {
                Optional<String> players = extractTextContent(translatableComponent, 2);
                if (players.isEmpty()) yield Optional.empty();

                Advancement advancement = Advancement.builder()
                        .advancementComponent(advancementComponent.get())
                        .criterion(criterion.get())
                        .players(players.get())
                        .build();

                yield Optional.of(advancement);
            }
            // Granted criterion '%s' of advancement %s to %s
            // Revoked criterion '%s' of advancement %s from %s
            case COMMANDS_ADVANCEMENT_GRANT_CRITERION_TO_ONE_SUCCESS, COMMANDS_ADVANCEMENT_REVOKE_CRITERION_TO_ONE_SUCCESS -> {
                Optional<FEntity> target = extractFEntity(translatableComponent, 2);
                if (target.isEmpty()) yield Optional.empty();

                Advancement advancement = Advancement.builder()
                        .advancementComponent(advancementComponent.get())
                        .criterion(criterion.get())
                        .target(target.get())
                        .build();

                yield Optional.of(advancement);
            }
            default -> Optional.empty();
        };
    }

    public Optional<Advancement> extractCommandAdvancement(MinecraftTranslationKey translationKey, TranslatableComponent translatableComponent) {
        return switch (translationKey) {
            // Successfully given all achievements to %s
            // Successfully taken all achievements from %s
            case COMMANDS_ACHIEVEMENT_GIVE_SUCCESS_ALL, COMMANDS_ACHIEVEMENT_TAKE_SUCCESS_ALL -> {
                Optional<FEntity> target = extractFEntity(translatableComponent, 0);
                if (target.isEmpty()) yield Optional.empty();

                Advancement advancement = Advancement.builder()
                        .advancementComponent(Component.empty())
                        .target(target.get())
                        .build();

                yield Optional.of(advancement);
            }
            // Granted %s advancements to %s players
            // Revoked %s advancements from %s players
            case COMMANDS_ADVANCEMENT_GRANT_MANY_TO_MANY_SUCCESS, COMMANDS_ADVANCEMENT_REVOKE_MANY_TO_MANY_SUCCESS -> {
                Optional<String> advancements = extractTextContent(translatableComponent, 0);
                if (advancements.isEmpty()) yield Optional.empty();

                Optional<String> players = extractTextContent(translatableComponent, 1);
                if (players.isEmpty()) yield Optional.empty();

                Advancement advancement = Advancement.builder()
                        .advancementComponent(Component.empty())
                        .advancements(advancements.get())
                        .players(players.get())
                        .build();

                yield Optional.of(advancement);
            }
            // Granted %s advancements to %s
            // Revoked %s advancements from %s
            case COMMANDS_ADVANCEMENT_GRANT_MANY_TO_ONE_SUCCESS, COMMANDS_ADVANCEMENT_REVOKE_MANY_TO_ONE_SUCCESS -> {
                Optional<String> advancements = extractTextContent(translatableComponent, 0);
                if (advancements.isEmpty()) yield Optional.empty();

                Optional<FEntity> target = extractFEntity(translatableComponent, 1);
                if (target.isEmpty()) yield Optional.empty();

                Advancement advancement = Advancement.builder()
                        .advancementComponent(Component.empty())
                        .advancements(advancements.get())
                        .target(target.get())
                        .build();

                yield Optional.of(advancement);
            }
            // Granted the advancement %s to %s players
            // Revoked the advancement %s from %s players
            case COMMANDS_ADVANCEMENT_GRANT_ONE_TO_MANY_SUCCESS, COMMANDS_ADVANCEMENT_REVOKE_ONE_TO_MANY_SUCCESS -> {
                Optional<Component> advancementComponent = getValueComponent(translatableComponent, 0);
                if (advancementComponent.isEmpty()) yield Optional.empty();

                Optional<String> players = extractTextContent(translatableComponent, 1);
                if (players.isEmpty()) yield Optional.empty();

                Advancement advancement = Advancement.builder()
                        .advancementComponent(advancementComponent.get())
                        .players(players.get())
                        .build();

                yield Optional.of(advancement);
            }
            // Granted the advancement %s to %s
            // Revoked the advancement %s from %s
            // Successfully given %s the stat %s
            // Successfully taken the stat %s from %s
            case COMMANDS_ADVANCEMENT_GRANT_ONE_TO_ONE_SUCCESS, COMMANDS_ADVANCEMENT_REVOKE_ONE_TO_ONE_SUCCESS,
                 COMMANDS_ACHIEVEMENT_GIVE_SUCCESS_ONE, COMMANDS_ACHIEVEMENT_TAKE_SUCCESS_ONE -> {
                Optional<Component> advancementComponent = getValueComponent(translatableComponent, 0);
                if (advancementComponent.isEmpty()) yield Optional.empty();

                Optional<FEntity> target = extractFEntity(translatableComponent, 1);
                if (target.isEmpty()) yield Optional.empty();

                Advancement advancement = Advancement.builder()
                        .advancementComponent(advancementComponent.get())
                        .target(target.get())
                        .build();

                yield Optional.of(advancement);
            }
            default -> Optional.empty();
        };
    }
}
