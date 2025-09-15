package net.flectone.pulse.module.message.sleep.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.module.message.sleep.model.Sleep;
import net.flectone.pulse.processing.extractor.Extractor;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
public class SleepExtractor extends Extractor {

    @Inject
    public SleepExtractor() {
    }

    public Optional<Sleep> extract(MinecraftTranslationKey translationKey, TranslatableComponent translatableComponent) {
        return switch (translationKey) {
            // %s/%s players sleeping
            case SLEEP_PLAYERS_SLEEPING -> {
                Optional<String> playersSleeping = extractTextContent(translatableComponent, 0);
                if (playersSleeping.isEmpty()) yield Optional.empty();

                Optional<String> players = extractTextContent(translatableComponent, 1);
                if (players.isEmpty()) yield Optional.empty();

                Sleep sleep = new Sleep(playersSleeping.get(), players.get());
                yield Optional.of(sleep);
            }
            case SLEEP_NOT_POSSIBLE, SLEEP_SKIPPING_NIGHT -> {
                Sleep sleep = new Sleep(null, null);
                yield Optional.of(sleep);
            }
            default -> Optional.empty();
        };
    }

}
