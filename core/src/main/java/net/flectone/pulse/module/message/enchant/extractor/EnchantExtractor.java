package net.flectone.pulse.module.message.enchant.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.message.enchant.model.Enchant;
import net.flectone.pulse.processing.extractor.Extractor;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
public class EnchantExtractor extends Extractor {

    @Inject
    public EnchantExtractor() {
    }

    public Optional<Enchant> extract(MinecraftTranslationKey translationKey, TranslatableComponent translatableComponent) {
        if (translationKey == MinecraftTranslationKey.COMMANDS_ENCHANT_SUCCESS) {
            Enchant enchant = Enchant.builder()
                    .name(Component.empty())
                    .players("")
                    .build();

            return Optional.of(enchant);
        }

        Optional<TranslatableComponent> name = getTranslatableComponent(translatableComponent, 0);
        if (name.isEmpty()) return Optional.empty();

        return switch (translationKey) {
            // Applied enchantment %s to %s entities
            case COMMANDS_ENCHANT_SUCCESS_MULTIPLE -> {
                Optional<String> players = extractTextContent(translatableComponent, 1);
                if (players.isEmpty()) yield Optional.empty();

                Enchant enchant = Enchant.builder()
                        .name(name.get())
                        .players(players.get())
                        .build();

                yield Optional.of(enchant);
            }
            // Applied enchantment %s to %s's item
            case COMMANDS_ENCHANT_SUCCESS_SINGLE -> {
                Optional<FEntity> target = extractFEntity(translatableComponent, 1);
                if (target.isEmpty()) yield Optional.empty();

                Enchant enchant = Enchant.builder()
                        .name(name.get())
                        .target(target.get())
                        .build();

                yield Optional.of(enchant);
            }
            default -> Optional.empty();
        };
    }

}
