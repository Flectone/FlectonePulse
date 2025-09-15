package net.flectone.pulse.module.message.teleport.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.message.teleport.model.TeleportEntity;
import net.flectone.pulse.module.message.teleport.model.TeleportLocation;
import net.flectone.pulse.processing.extractor.Extractor;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
public class TeleportExtractor extends Extractor {

    @Inject
    public TeleportExtractor() {
    }

    public Optional<TeleportEntity> extractEntity(MinecraftTranslationKey translationKey, TranslatableComponent translatableComponent) {
        Optional<FEntity> secondTarget = extractFEntity(translatableComponent, 1);
        if (secondTarget.isEmpty()) return Optional.empty();

        return switch (translationKey) {
            // Teleported %s entities to %s
            case COMMANDS_TELEPORT_SUCCESS_ENTITY_MULTIPLE -> {
                Optional<String> entities = extractTextContent(translatableComponent, 0);
                if (entities.isEmpty()) yield Optional.empty();

                TeleportEntity teleportEntity = TeleportEntity.builder()
                        .secondTarget(secondTarget.get())
                        .entities(entities.get())
                        .build();

                yield Optional.of(teleportEntity);
            }
            // Teleported %s to %s
            case COMMANDS_TELEPORT_SUCCESS_ENTITY_SINGLE -> {
                Optional<FEntity> target = extractFEntity(translatableComponent, 0);
                if (target.isEmpty()) yield Optional.empty();

                TeleportEntity teleportEntity = TeleportEntity.builder()
                        .secondTarget(secondTarget.get())
                        .target(target.get())
                        .build();

                yield Optional.of(teleportEntity);
            }
            default -> Optional.empty();
        };
    }

    public Optional<TeleportLocation> extractLocation(MinecraftTranslationKey translationKey, TranslatableComponent translatableComponent) {
        Optional<String> x = extractTextContent(translatableComponent, 1);
        if (x.isEmpty()) return Optional.empty();

        Optional<String> y = extractTextContent(translatableComponent, 2);
        if (y.isEmpty()) return Optional.empty();

        Optional<String> z = extractTextContent(translatableComponent, 3);
        if (z.isEmpty()) return Optional.empty();

        return switch (translationKey) {
            // Teleported %s entities to %s, %s, %s
            case COMMANDS_TELEPORT_SUCCESS_LOCATION_MULTIPLE -> {
                Optional<String> entities = extractTextContent(translatableComponent, 0);
                if (entities.isEmpty()) yield Optional.empty();

                TeleportLocation teleportLocation = TeleportLocation.builder()
                        .x(x.get())
                        .y(y.get())
                        .z(z.get())
                        .entities(entities.get())
                        .build();

                yield Optional.of(teleportLocation);
            }
            // Teleported %s to %s, %s, %s
            case COMMANDS_TELEPORT_SUCCESS_LOCATION_SINGLE -> {
                Optional<FEntity> target = extractFEntity(translatableComponent, 0);
                if (target.isEmpty()) yield Optional.empty();

                TeleportLocation teleportLocation = TeleportLocation.builder()
                        .x(x.get())
                        .y(y.get())
                        .z(z.get())
                        .target(target.get())
                        .build();

                yield Optional.of(teleportLocation);
            }
            default -> Optional.empty();
        };
    }
}
