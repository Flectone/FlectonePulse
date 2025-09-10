package net.flectone.pulse.module.message.give.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.message.give.model.Give;
import net.flectone.pulse.processing.extractor.Extractor;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.TranslationArgument;

import java.util.List;
import java.util.Optional;

@Singleton
public class GiveExtractor extends Extractor {

    @Inject
    public GiveExtractor() {
    }

    public Optional<Give> extract(MinecraftTranslationKey translationKey, TranslatableComponent translatableComponent) {
        List<TranslationArgument> translationArguments = translatableComponent.arguments();
        if (translationArguments.size() < 3) return Optional.empty();
        if (!(translationArguments.get(0).asComponent() instanceof TextComponent amountComponent)) return Optional.empty();
        if (!(translationArguments.get(1).asComponent() instanceof TranslatableComponent emptyItemComponent)) return Optional.empty();
        if (emptyItemComponent.arguments().isEmpty()) return Optional.empty();
        if (!(emptyItemComponent.arguments().getFirst().asComponent() instanceof TextComponent itemComponent)) return Optional.empty();
        if (itemComponent.children().isEmpty()) return Optional.empty();

        String itemName;
        if (itemComponent.children().getFirst().asComponent() instanceof TextComponent textComponent) {
            itemName = textComponent.content();
        } else if (itemComponent.children().getFirst().asComponent() instanceof TranslatableComponent translatableComponent1) {
            itemName = translatableComponent1.key();
        } else {
            return Optional.empty();
        }

        String amount = amountComponent.content();

        Component component = translationArguments.get(2).asComponent();

        if (translationKey == MinecraftTranslationKey.COMMANDS_EFFECT_GIVE_SUCCESS_MULTIPLE) {
            if (!(component instanceof TextComponent countComponent)) return Optional.empty();

            String count = countComponent.content();
            Give give = new Give(amount, itemName, null, count);
            return Optional.of(give);
        }

        Optional<FEntity> optionalFEntity = extractFEntity(component);
        if (optionalFEntity.isEmpty()) return Optional.empty();

        Give give = new Give(amount, itemName, optionalFEntity.get(), null);
        return Optional.of(give);
    }
}