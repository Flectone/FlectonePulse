package net.flectone.pulse.module.message.enchant.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.enchant.model.Enchant;
import net.flectone.pulse.processing.extractor.Extractor;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
public class EnchantExtractor extends Extractor {

    @Inject
    public EnchantExtractor() {
    }

    public Optional<Enchant> extract(MessageReceiveEvent event) {
        MinecraftTranslationKey translationKey = event.getTranslationKey();
        if (!translationKey.startsWith("commands.enchant.success")) return Optional.empty();
        if (translationKey == MinecraftTranslationKey.COMMANDS_ENCHANT_SUCCESS) {
            Enchant enchant = new Enchant("", "",  "", null);
            return Optional.of(enchant);
        }

        TranslatableComponent translatableComponent = event.getTranslatableComponent();
        if (translatableComponent.arguments().size() < 2) return Optional.empty();
        if (!(translatableComponent.arguments().getFirst().asComponent() instanceof TranslatableComponent enchantComponent)) return Optional.empty();
        String enchantKey = enchantComponent.key();

        if (enchantComponent.children().size() < 2) return Optional.empty();
        if (!(enchantComponent.children().get(1) instanceof TranslatableComponent levelComponent)) return Optional.empty();
        String levelKey = levelComponent.key();

        if (translationKey == MinecraftTranslationKey.COMMANDS_ENCHANT_SUCCESS_MULTIPLE) {
            if (translatableComponent.arguments().get(1).asComponent() instanceof TextComponent targetComponent) {
                Enchant enchant = new Enchant(enchantKey, levelKey, targetComponent.content(), null);
                return Optional.of(enchant);
            }

            return Optional.empty();
        }

        Optional<FEntity> optionalFEntity = extractFEntity(translatableComponent.arguments().get(1).asComponent());
        if (optionalFEntity.isEmpty()) return Optional.empty();

        Enchant enchant = new Enchant(enchantKey, levelKey, "", optionalFEntity.get());
        return Optional.of(enchant);
    }

}
