package net.flectone.pulse.module.message.sound.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.message.sound.model.Sound;
import net.flectone.pulse.processing.extractor.Extractor;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SoundExtractor extends Extractor {

    public Optional<Sound> extract(MinecraftTranslationKey translationKey, TranslatableComponent translatableComponent) {
        return switch (translationKey) {
            // Played sound %s to %s players
            case COMMANDS_PLAYSOUND_SUCCESS_MULTIPLE -> {
                Optional<String> name = extractTextContent(translatableComponent, 0);
                if (name.isEmpty()) yield Optional.empty();

                Optional<String> players = extractTextContent(translatableComponent, 1);
                if (players.isEmpty()) yield Optional.empty();

                Sound sound = Sound.builder()
                        .name(name.get())
                        .players(players.get())
                        .build();

                yield Optional.of(sound);
            }
            // Stopped all sounds for %s
            case COMMANDS_STOPSOUND_SUCCESS_ALL -> {
                Optional<FEntity> target = extractFEntity(translatableComponent, 0);
                if (target.isEmpty()) yield Optional.empty();

                Sound sound = Sound.builder()
                        .target(target.get())
                        .build();

                yield Optional.of(sound);
            }
            // Stopped source '%s' for %s
            case COMMANDS_STOPSOUND_SUCCESS_SOUNDSOURCE -> {
                Optional<String> source = extractTextContent(translatableComponent, 0);
                if (source.isEmpty()) yield Optional.empty();

                Optional<FEntity> target = extractFEntity(translatableComponent, 1);
                if (target.isEmpty()) yield Optional.empty();

                Sound sound = Sound.builder()
                        .source(source.get())
                        .target(target.get())
                        .build();

                yield Optional.of(sound);
            }
            // Stopped sound '%s' with source '%s' for %s
            case COMMANDS_STOPSOUND_SUCCESS_INDIVIDUALSOUND -> {
                Optional<String> name = extractTextContent(translatableComponent, 0);
                if (name.isEmpty()) yield Optional.empty();

                Optional<String> source = extractTextContent(translatableComponent, 1);
                if (source.isEmpty()) yield Optional.empty();

                Optional<FEntity> target = extractFEntity(translatableComponent, 2);
                if (target.isEmpty()) yield Optional.empty();

                Sound sound = Sound.builder()
                        .name(name.get())
                        .source(source.get())
                        .target(target.get())
                        .build();

                yield Optional.of(sound);
            }
            // Played sound %s to %s
            // Played sound '%s' to %s
            case COMMANDS_PLAYSOUND_SUCCESS_SINGLE, COMMANDS_PLAYSOUND_SUCCESS -> {
                Optional<String> name = extractTextContent(translatableComponent, 0);
                if (name.isEmpty()) yield Optional.empty();

                Optional<FEntity> target = extractFEntity(translatableComponent, 1);
                if (target.isEmpty()) yield Optional.empty();

                Sound sound = Sound.builder()
                        .name(name.get())
                        .target(target.get())
                        .build();

                yield Optional.of(sound);
            }
            // Stopped all '%s' sounds
            case COMMANDS_STOPSOUND_SUCCESS_SOURCE_ANY -> {
                Optional<String> source = extractTextContent(translatableComponent, 0);
                if (source.isEmpty()) yield Optional.empty();

                Sound sound = Sound.builder()
                        .source(source.get())
                        .build();

                yield Optional.of(sound);
            }
            // Stopped sound '%s' on source '%s'
            case COMMANDS_STOPSOUND_SUCCESS_SOURCE_SOUND -> {
                Optional<String> name = extractTextContent(translatableComponent, 0);
                if (name.isEmpty()) yield Optional.empty();

                Optional<String> source = extractTextContent(translatableComponent, 1);
                if (source.isEmpty()) yield Optional.empty();

                Sound sound = Sound.builder()
                        .name(name.get())
                        .source(source.get())
                        .build();

                yield Optional.of(sound);
            }
            // Stopped all sounds
            case COMMANDS_STOPSOUND_SUCCESS_SOURCELESS_ANY -> Optional.of(Sound.builder().build());
            // Stopped sound '%s'
            case COMMANDS_STOPSOUND_SUCCESS_SOURCELESS_SOUND -> {
                Optional<String> name = extractTextContent(translatableComponent, 0);
                if (name.isEmpty()) yield Optional.empty();

                Sound sound = Sound.builder()
                        .name(name.get())
                        .build();

                yield Optional.of(sound);
            }
            default -> Optional.empty();
        };
    }

}
