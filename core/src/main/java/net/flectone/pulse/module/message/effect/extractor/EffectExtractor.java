package net.flectone.pulse.module.message.effect.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.message.effect.model.Effect;
import net.flectone.pulse.processing.extractor.Extractor;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
public class EffectExtractor extends Extractor {

    @Inject
    public EffectExtractor() {
    }

    public Optional<Effect> extract(MinecraftTranslationKey translationKey, TranslatableComponent translatableComponent) {
        return switch (translationKey) {
            // Removed every effect from %s targets
            case COMMANDS_EFFECT_CLEAR_EVERYTHING_SUCCESS_MULTIPLE -> {
                Optional<String> players = extractTextContent(translatableComponent, 0);
                if (players.isEmpty()) yield Optional.empty();

                Effect effect = Effect.builder()
                        .players(players.get())
                        .build();

                yield Optional.of(effect);
            }
            // Removed every effect from %s
            case COMMANDS_EFFECT_CLEAR_EVERYTHING_SUCCESS_SINGLE -> {
                Optional<FEntity> target = extractFEntity(translatableComponent, 0);
                if (target.isEmpty()) yield Optional.empty();

                Effect effect = Effect.builder()
                        .target(target.get())
                        .build();

                yield Optional.of(effect);
            }
            // Applied effect %s to %s targets
            // Removed effect %s from %s targets
            case COMMANDS_EFFECT_GIVE_SUCCESS_MULTIPLE, COMMANDS_EFFECT_CLEAR_SPECIFIC_SUCCESS_MULTIPLE -> {
                Optional<String> name = extractTranslatableKey(translatableComponent, 0);
                if (name.isEmpty()) yield Optional.empty();

                Optional<String> players = extractTextContent(translatableComponent, 1);
                if (players.isEmpty()) yield Optional.empty();

                Effect effect = Effect.builder()
                        .name(name.get())
                        .players(players.get())
                        .build();

                yield Optional.of(effect);
            }
            // Applied effect %s to %s
            // Removed effect %s from %s
            case COMMANDS_EFFECT_GIVE_SUCCESS_SINGLE, COMMANDS_EFFECT_CLEAR_SPECIFIC_SUCCESS_SINGLE -> {
                Optional<String> name = extractTranslatableKey(translatableComponent, 0);
                if (name.isEmpty()) yield Optional.empty();

                Optional<FEntity> target = extractFEntity(translatableComponent, 1);
                if (target.isEmpty()) yield Optional.empty();

                Effect effect = Effect.builder()
                        .name(name.get())
                        .target(target.get())
                        .build();

                yield Optional.of(effect);
            }
            default -> Optional.empty();
        };
    }
}
