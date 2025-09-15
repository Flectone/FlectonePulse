package net.flectone.pulse.module.message.spawn.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.message.spawn.model.Spawn;
import net.flectone.pulse.processing.extractor.Extractor;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
public class SpawnExtractor extends Extractor {

    @Inject
    public SpawnExtractor() {
    }

    public Optional<Spawn> extract(MinecraftTranslationKey translationKey, TranslatableComponent translatableComponent) {
        if (translationKey == MinecraftTranslationKey.COMMANDS_SPAWNPOINT_SUCCESS) {
            Optional<FEntity> target = extractFEntity(translatableComponent, 0);
            if (target.isEmpty()) return Optional.empty();

            Optional<String> x = extractTextContent(translatableComponent, 1);
            if (x.isEmpty()) return Optional.empty();

            Optional<String> y = extractTextContent(translatableComponent, 2);
            if (y.isEmpty()) return Optional.empty();

            Optional<String> z = extractTextContent(translatableComponent, 3);
            if (z.isEmpty()) return Optional.empty();

            Spawn spawn = Spawn.builder()
                    .x(x.get())
                    .y(y.get())
                    .z(z.get())
                    .target(target.get())
                    .build();

            return Optional.of(spawn);
        }

        Optional<String> x = extractTextContent(translatableComponent, 0);
        if (x.isEmpty()) return Optional.empty();

        Optional<String> y = extractTextContent(translatableComponent, 1);
        if (y.isEmpty()) return Optional.empty();

        Optional<String> z = extractTextContent(translatableComponent, 2);
        if (z.isEmpty()) return Optional.empty();

        Optional<String> angle = extractTextContent(translatableComponent, 3);
        if (angle.isEmpty()) return Optional.empty();

        Spawn.SpawnBuilder spawnBuilder = Spawn.builder()
                .x(x.get())
                .y(y.get())
                .z(z.get())
                .angle(angle.get());

        return switch (translationKey) {
            // Set the world spawn point to %s, %s, %s [%s]
            case COMMANDS_SETWORLDSPAWN_SUCCESS -> Optional.of(spawnBuilder.build());
            // Set spawn point to %s, %s, %s [%s] in %s for %s players
            case COMMANDS_SPAWNPOINT_SUCCESS_MULTIPLE -> {
                Optional<String> world = extractTextContent(translatableComponent, 4);
                if (world.isEmpty()) yield Optional.empty();

                Optional<String> players = extractTextContent(translatableComponent, 5);
                if (players.isEmpty()) yield Optional.empty();

                Spawn spawn = spawnBuilder
                        .world(world.get())
                        .players(players.get())
                        .build();

                yield Optional.of(spawn);
            }
            // Set spawn point to %s, %s, %s [%s] in %s for %s
            case COMMANDS_SPAWNPOINT_SUCCESS_SINGLE -> {
                Optional<String> world = extractTextContent(translatableComponent, 4);
                if (world.isEmpty()) yield Optional.empty();

                Optional<FEntity> target = extractFEntity(translatableComponent, 5);
                if (target.isEmpty()) yield Optional.empty();

                Spawn spawn = spawnBuilder
                        .world(world.get())
                        .target(target.get())
                        .build();

                yield Optional.of(spawn);
            }
            default -> Optional.empty();
        };
    }

}
