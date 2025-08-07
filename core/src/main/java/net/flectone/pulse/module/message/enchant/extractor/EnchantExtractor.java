package net.flectone.pulse.module.message.enchant.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.enchant.model.Enchant;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
public class EnchantExtractor {

    @Inject
    public EnchantExtractor() {
    }

    public Optional<Enchant> extract(MessageReceiveEvent event) {
        MinecraftTranslationKey translationKey = event.getTranslationKey();
        if (!translationKey.startsWith("commands.enchant.success")) return Optional.empty();
        if (translationKey == MinecraftTranslationKey.COMMANDS_ENCHANT_SUCCESS) {
            Enchant enchant = new Enchant("", "", "");
            return Optional.of(enchant);
        }

        TranslatableComponent translatableComponent = event.getTranslatableComponent();
        if (translatableComponent.args().size() < 2) return Optional.empty();
        if (!(translatableComponent.args().get(0) instanceof TranslatableComponent enchantComponent)) return Optional.empty();
        String enchantKey = enchantComponent.key();

        if (enchantComponent.children().size() < 2) return Optional.empty();
        if (!(enchantComponent.children().get(1) instanceof TranslatableComponent levelComponent)) return Optional.empty();
        String levelKey = levelComponent.key();

        if (!(translatableComponent.args().get(1) instanceof TextComponent targetComponent)) return Optional.empty();
        String target = targetComponent.content();

        Enchant enchant = new Enchant(enchantKey, levelKey, target);
        return Optional.of(enchant);
    }

}
