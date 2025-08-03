package net.flectone.pulse.module.message.enchant.extractor;

import net.flectone.pulse.model.event.message.TranslatableMessageReceiveEvent;
import net.flectone.pulse.module.message.enchant.model.Enchant;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

public class EnchantExtractor {

    public Optional<Enchant> extractEnchant(TranslatableMessageReceiveEvent event) {
        if (!event.getKey().startsWith("commands.enchant.success")) return Optional.empty();
        if (event.getKey() == MinecraftTranslationKey.COMMANDS_ENCHANT_SUCCESS) {
            Enchant enchant = new Enchant("", "", "");
            return Optional.of(enchant);
        }

        TranslatableComponent translatableComponent = event.getComponent();
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
