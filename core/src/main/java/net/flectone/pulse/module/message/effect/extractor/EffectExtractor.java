package net.flectone.pulse.module.message.effect.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.message.effect.model.Effect;
import net.flectone.pulse.processing.extractor.Extractor;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
public class EffectExtractor extends Extractor {

    @Inject
    public EffectExtractor() {
    }

    public Optional<Effect> extractTarget(MinecraftTranslationKey translationKey, TranslatableComponent translatableComponent) {
        if (translatableComponent.arguments().isEmpty()) return Optional.empty();

        Component component = translatableComponent.arguments().getFirst().asComponent();

        if (translationKey == MinecraftTranslationKey.COMMANDS_EFFECT_CLEAR_EVERYTHING_SUCCESS_MULTIPLE) {
            if (!(component instanceof TextComponent countComponent)) return Optional.empty();

            Effect effect = new Effect(null, null, countComponent.content());
            return Optional.of(effect);
        }

        Optional<FEntity> optionalFEntity = extractFEntity(component);
        if (optionalFEntity.isEmpty()) return Optional.empty();

        Effect effect = new Effect(null, optionalFEntity.get(), null);
        return Optional.of(effect);
    }

    public Optional<Effect> extractNameAndTarget(MinecraftTranslationKey translationKey, TranslatableComponent translatableComponent) {
        if (translatableComponent.arguments().size() < 2) return Optional.empty();
        if (!(translatableComponent.arguments().get(0).asComponent() instanceof TranslatableComponent effectComponent)) return Optional.empty();

        String name = effectComponent.key();

        Component secondComponent = translatableComponent.arguments().get(1).asComponent();

        if (translationKey == MinecraftTranslationKey.COMMANDS_EFFECT_GIVE_SUCCESS_MULTIPLE
                || translationKey == MinecraftTranslationKey.COMMANDS_EFFECT_CLEAR_SPECIFIC_SUCCESS_MULTIPLE) {
            if (!(secondComponent instanceof TextComponent countComponent)) return Optional.empty();

            Effect effect = new Effect(name, null, countComponent.content());
            return Optional.of(effect);
        }

        Optional<FEntity> optionalFEntity = extractFEntity(secondComponent);
        if (optionalFEntity.isEmpty()) return Optional.empty();

        Effect effect = new Effect(name, optionalFEntity.get(), null);
        return Optional.of(effect);
    }
}
