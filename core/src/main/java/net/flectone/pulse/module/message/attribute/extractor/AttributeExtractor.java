package net.flectone.pulse.module.message.attribute.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.message.attribute.model.Attribute;
import net.flectone.pulse.processing.extractor.Extractor;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.TranslationArgument;

import java.util.List;
import java.util.Optional;

@Singleton
public class AttributeExtractor extends Extractor {

    @Inject
    public AttributeExtractor() {
    }

    public Optional<Attribute> extractBaseValue(TranslatableComponent translatableComponent) {
        List<TranslationArgument> translationArguments = translatableComponent.arguments();
        if (translationArguments.size() < 3) return Optional.empty();
        if (!(translationArguments.get(0).asComponent() instanceof TranslatableComponent attributeComponent)) return Optional.empty();

        Optional<FEntity> optionalFEntity = extractFEntity(translationArguments.get(1).asComponent());
        if (optionalFEntity.isEmpty()) return Optional.empty();

        if (!(translationArguments.get(2).asComponent() instanceof TextComponent valueComponent)) return Optional.empty();

        String name = attributeComponent.key();
        String value = valueComponent.content();
        Attribute attribute = new Attribute(optionalFEntity.get(), name, null, value);
        return Optional.of(attribute);
    }

    public Optional<Attribute> extractModifier(MinecraftTranslationKey translationKey, TranslatableComponent translatableComponent) {
        List<TranslationArgument> translationArguments = translatableComponent.arguments();
        if (translationArguments.size() < 3) return Optional.empty();
        if (!(translationArguments.get(0).asComponent() instanceof TextComponent modifierComponent)) return Optional.empty();
        if (!(translationArguments.get(1).asComponent() instanceof TranslatableComponent attributeComponent)) return Optional.empty();

        Optional<FEntity> optionalFEntity = extractFEntity(translationArguments.get(2).asComponent());
        if (optionalFEntity.isEmpty()) return Optional.empty();

        String value = null;
        if (translationKey == MinecraftTranslationKey.COMMANDS_ATTRIBUTE_MODIFIER_VALUE_GET_SUCCESS) {
            if (translationArguments.size() < 4) return Optional.empty();
            if (!(translationArguments.get(3).asComponent() instanceof TextComponent valueComponent)) return Optional.empty();

            value = valueComponent.content();
        }

        String name = attributeComponent.key();
        String modifier = modifierComponent.content();

        Attribute attribute = new Attribute(optionalFEntity.get(), name, modifier, value);
        return Optional.of(attribute);
    }

}