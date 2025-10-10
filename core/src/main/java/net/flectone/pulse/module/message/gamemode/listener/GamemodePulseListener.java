package net.flectone.pulse.module.message.gamemode.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.gamemode.GamemodeModule;
import net.flectone.pulse.module.message.gamemode.extractor.GamemodeExtractor;
import net.flectone.pulse.module.message.gamemode.model.Gamemode;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class GamemodePulseListener implements PulseListener {

    private final GamemodeModule gamemodeModule;
    private final GamemodeExtractor gamemodeExtractor;

    @Pulse
    public void onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        MinecraftTranslationKey translationKey = event.getTranslationKey();
        switch (translationKey) {
            case COMMANDS_DEFAULTGAMEMODE_SUCCESS, GAMEMODE_CHANGED,
                 COMMANDS_GAMEMODE_SUCCESS_OTHER, COMMANDS_GAMEMODE_SUCCESS_SELF -> {
                Optional<Gamemode> gamemode = gamemodeExtractor.extract(translationKey, event.getTranslatableComponent());
                if (gamemode.isEmpty()) return;

                Gamemode finalGamemode = gamemode.get().getTarget() != null
                        ? gamemode.get()
                        : Gamemode.builder()
                        .name(gamemode.get().getName())
                        .target(event.getFPlayer())
                        .build();

                event.setCancelled(true);
                gamemodeModule.send(event.getFPlayer(), translationKey, finalGamemode);
            }
        }
    }

}
