package net.flectone.pulse.module.message.kill.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.message.kill.model.Kill;
import net.flectone.pulse.processing.extractor.Extractor;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class KillExtractor extends Extractor {

    public Optional<Kill> extract(MinecraftTranslationKey translationKey, TranslatableComponent translatableComponent) {
        return switch (translationKey) {
            // Killed %s entities
            case COMMANDS_KILL_SUCCESS_MULTIPLE -> {
                Optional<String> entities = extractTextContent(translatableComponent, 0);
                if (entities.isEmpty()) yield Optional.empty();

                Kill kill = Kill.builder()
                        .entities(entities.get())
                        .build();

                yield Optional.of(kill);
            }
            // Killed %s
            case COMMANDS_KILL_SUCCESS_SINGLE, COMMANDS_KILL_SUCCESSFUL -> {
                Optional<FEntity> target = extractFEntity(translatableComponent, 0);
                if (target.isEmpty()) yield Optional.empty();

                Kill kill = Kill.builder()
                        .target(target.get())
                        .build();

                yield Optional.of(kill);
            }
            default -> Optional.empty();
        };
    }
}
