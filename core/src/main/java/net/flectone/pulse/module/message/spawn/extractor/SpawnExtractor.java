package net.flectone.pulse.module.message.spawn.extractor;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.message.spawn.model.Spawn;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.processing.extractor.Extractor;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
public class SpawnExtractor extends Extractor {

    private final PacketProvider packetProvider;

    @Inject
    public SpawnExtractor(PacketProvider packetProvider) {
        this.packetProvider = packetProvider;
    }

    public Optional<Spawn> extract(MinecraftTranslationKey translationKey, TranslatableComponent translatableComponent) {
        // Set %s's spawn point to (%d, %d, %d)
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

        Spawn.SpawnBuilder spawnBuilder = Spawn.builder()
                .x(x.get())
                .y(y.get())
                .z(z.get());

        if (packetProvider.getServerVersion().isOlderThan(ServerVersion.V_1_16)
                && translationKey == MinecraftTranslationKey.COMMANDS_SETWORLDSPAWN_SUCCESS) {
            return Optional.of(spawnBuilder.build());
        }

        if (packetProvider.getServerVersion().isOlderThanOrEquals(ServerVersion.V_1_14_2)) {
            return extract1_14_2(spawnBuilder, translationKey, translatableComponent);
        } else {
            return extract1_16_5(spawnBuilder, translationKey, translatableComponent);
        }
    }

    private Optional<Spawn> extract1_16_5(Spawn.SpawnBuilder spawnBuilder,
                                          MinecraftTranslationKey translationKey,
                                          TranslatableComponent translatableComponent) {
        Optional<String> angle = extractTextContent(translatableComponent, 3);
        if (angle.isEmpty()) return Optional.empty();

        spawnBuilder = spawnBuilder.angle(angle.get());

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

    private Optional<Spawn> extract1_14_2(Spawn.SpawnBuilder spawnBuilder,
                                          MinecraftTranslationKey translationKey,
                                          TranslatableComponent translatableComponent) {
        return switch (translationKey) {
            // Set the world spawn point to %s, %s, %s [%s]
            case COMMANDS_SETWORLDSPAWN_SUCCESS -> Optional.of(spawnBuilder.build());
            // Set spawn point to %s, %s, %s for %s players
            case COMMANDS_SPAWNPOINT_SUCCESS_MULTIPLE -> {
                Optional<String> players = extractTextContent(translatableComponent, 3);
                if (players.isEmpty()) yield Optional.empty();

                Spawn spawn = spawnBuilder
                        .players(players.get())
                        .build();

                yield Optional.of(spawn);
            }
            // Set spawn point to %s, %s, %s for %s
            case COMMANDS_SPAWNPOINT_SUCCESS_SINGLE -> {
                Optional<FEntity> target = extractFEntity(translatableComponent, 3);
                if (target.isEmpty()) yield Optional.empty();

                Spawn spawn = spawnBuilder
                        .target(target.get())
                        .build();

                yield Optional.of(spawn);
            }
            default -> Optional.empty();
        };
    }

}
