package net.flectone.pulse.module.message.gamemode.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.gamemode.GamemodeModule;
import net.flectone.pulse.module.message.gamemode.extractor.GamemodeExtractor;
import net.flectone.pulse.module.message.gamemode.model.Gamemode;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

import java.util.Optional;

@Singleton
public class GamemodePulseListener implements PulseListener {

    private final GamemodeModule gamemodeModule;
    private final GamemodeExtractor gamemodeExtractor;

    @Inject
    public GamemodePulseListener(GamemodeModule gamemodeModule,
                                 GamemodeExtractor gamemodeExtractor) {
        this.gamemodeModule = gamemodeModule;
        this.gamemodeExtractor = gamemodeExtractor;
    }

    @Pulse
    public void onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        MinecraftTranslationKey key = event.getTranslationKey();
        if (!key.startsWith("commands.gamemode.success")
                && key != MinecraftTranslationKey.GAMEMODE_CHANGED
                && key != MinecraftTranslationKey.COMMANDS_DEFAULTGAMEMODE_SUCCESS) return;

        Optional<Gamemode> gamemode = gamemodeExtractor.extract(event);
        if (gamemode.isEmpty()) return;

        event.setCancelled(true);
        gamemodeModule.send(event.getFPlayer(), gamemode.get());
    }

}
