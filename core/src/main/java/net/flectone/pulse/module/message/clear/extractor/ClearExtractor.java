package net.flectone.pulse.module.message.clear.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.message.clear.model.Clear;
import net.flectone.pulse.processing.extractor.Extractor;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
public class ClearExtractor extends Extractor {

    @Inject
    public ClearExtractor() {
    }

    public Optional<Clear> extract(MinecraftTranslationKey translationKey, TranslatableComponent translatableComponent) {
        Optional<String> items = extractTextContent(translatableComponent, 0);
        if (items.isEmpty()) return Optional.empty();

        return switch (translationKey) {
            // Removed %s item(s) from %s players
            case COMMANDS_CLEAR_SUCCESS_MULTIPLE -> {
                Optional<String> players = extractTextContent(translatableComponent, 1);
                if (players.isEmpty()) yield Optional.empty();

                Clear clear = Clear.builder()
                        .items(items.get())
                        .players(players.get())
                        .build();

                yield Optional.of(clear);
            }
            // Removed %s item(s) from player %s
            case COMMANDS_CLEAR_SUCCESS_SINGLE, COMMANDS_CLEAR_SUCCESS -> {
                Optional<FEntity> target = extractFEntity(translatableComponent, 1);
                if (target.isEmpty()) yield Optional.empty();

                Clear clear = Clear.builder()
                        .items(items.get())
                        .target(target.get())
                        .build();

                yield Optional.of(clear);
            }
            default -> Optional.empty();
        };
    }

}
