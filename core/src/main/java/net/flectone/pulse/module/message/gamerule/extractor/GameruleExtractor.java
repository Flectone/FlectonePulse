package net.flectone.pulse.module.message.gamerule.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.module.message.gamerule.model.Gamerule;
import net.flectone.pulse.processing.extractor.Extractor;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
public class GameruleExtractor extends Extractor {

    @Inject
    public GameruleExtractor() {
    }

    public Optional<Gamerule> extract(MinecraftTranslationKey translationKey, TranslatableComponent translatableComponent) {
        return switch (translationKey) {
            // Gamerule %s is currently set to: %s
            // Gamerule %s is now set to: %s
            case COMMANDS_GAMERULE_QUERY, COMMANDS_GAMERULE_SET -> {
                Optional<String> name = extractTextContent(translatableComponent, 0);
                if (name.isEmpty()) yield Optional.empty();

                Optional<String> value = extractTextContent(translatableComponent, 1);
                if (value.isEmpty()) yield Optional.empty();

                Gamerule gamerule = new Gamerule(name.get(), value.get());
                yield Optional.of(gamerule);
            }
            // Game rule has been updated
            case COMMANDS_GAMERULE_SUCCESS -> {
                Gamerule gamerule = new Gamerule("", "");
                yield Optional.of(gamerule);
            }
            default -> Optional.empty();
        };
    }
}
