package net.flectone.pulse.module.message.attribute.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.message.attribute.model.Attribute;
import net.flectone.pulse.processing.extractor.Extractor;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
public class AttributeExtractor extends Extractor {

    @Inject
    public AttributeExtractor() {
    }

    // Base value of attribute %s for entity %s is %s
    // Base value for attribute %s for entity %s reset to default %s
    // Base value for attribute %s for entity %s set to %s
    // Value of attribute %s for entity %s is %s
    public Optional<Attribute> extractBaseValue(TranslatableComponent translatableComponent) {
        Optional<String> attributeName = extractTranslatableKey(translatableComponent, 0);
        if (attributeName.isEmpty()) return Optional.empty();

        Optional<FEntity> target = extractFEntity(translatableComponent, 1);
        if (target.isEmpty()) return Optional.empty();

        Optional<String> value = extractTextContent(translatableComponent, 2);
        if (value.isEmpty()) return Optional.empty();

        Attribute attribute = Attribute.builder()
                .name(attributeName.get())
                .target(target.get())
                .value(value.get())
                .build();

        return Optional.of(attribute);
    }

    // Added modifier %s to attribute %s for entity %s
    // Removed modifier %s from attribute %s for entity %s
    public Optional<Attribute> extractModifier(MinecraftTranslationKey translationKey, TranslatableComponent translatableComponent) {
        Optional<String> modifier = extractTextContent(translatableComponent, 0);
        if (modifier.isEmpty()) return Optional.empty();

        Optional<String> attributeName = extractTranslatableKey(translatableComponent, 1);
        if (attributeName.isEmpty()) return Optional.empty();

        Optional<FEntity> target = extractFEntity(translatableComponent, 2);
        if (target.isEmpty()) return Optional.empty();

        Optional<String> value = Optional.empty();

        // Value of modifier %s on attribute %s for entity %s is %s
        if (translationKey == MinecraftTranslationKey.COMMANDS_ATTRIBUTE_MODIFIER_VALUE_GET_SUCCESS) {
            value = extractTextContent(translatableComponent, 3);
        }

        Attribute attribute = Attribute.builder()
                .modifier(modifier.get())
                .name(attributeName.get())
                .target(target.get())
                .value(value.orElse(null))
                .build();

        return Optional.of(attribute);
    }

}