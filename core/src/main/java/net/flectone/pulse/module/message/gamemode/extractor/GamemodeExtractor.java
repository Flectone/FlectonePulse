package net.flectone.pulse.module.message.gamemode.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.message.gamemode.model.Gamemode;
import net.flectone.pulse.processing.extractor.Extractor;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class GamemodeExtractor extends Extractor {

    public Optional<Gamemode> extract(MinecraftTranslationKey translationKey, TranslatableComponent translatableComponent) {
        return switch (translationKey) {
            case GAMEMODE_CHANGED -> {
                Gamemode gamemode = Gamemode.builder().build();
                yield Optional.of(gamemode);
            }
            // Set %s's game mode to %s
            case COMMANDS_GAMEMODE_SUCCESS_OTHER -> {
                Optional<FEntity> target = extractFEntity(translatableComponent, 0);
                if (target.isEmpty()) yield Optional.empty();

                Optional<String> name = extractTranslatableKey(translatableComponent, 1);
                if (name.isEmpty()) yield Optional.empty();

                Gamemode gamemode = Gamemode.builder()
                        .target(target.get())
                        .name(name.get())
                        .build();

                yield Optional.of(gamemode);
            }
            // Set own game mode to %s
            // The default game mode is now %s
            case COMMANDS_GAMEMODE_SUCCESS_SELF, COMMANDS_DEFAULTGAMEMODE_SUCCESS -> {
                Optional<String> name = extractTranslatableKey(translatableComponent, 0);
                if (name.isEmpty()) yield Optional.empty();

                Gamemode gamemode = Gamemode.builder()
                        .name(name.get())
                        .build();

                yield Optional.of(gamemode);
            }
            default -> Optional.empty();
        };
    }

}
