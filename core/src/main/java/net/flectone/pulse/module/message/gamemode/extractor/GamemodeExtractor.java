package net.flectone.pulse.module.message.gamemode.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.event.message.TranslatableMessageReceiveEvent;
import net.flectone.pulse.module.message.gamemode.model.Gamemode;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
public class GamemodeExtractor {

    @Inject
    public GamemodeExtractor() {
    }

    public Optional<Gamemode> extract(TranslatableMessageReceiveEvent event) {
        String target = event.getFPlayer().getName();
        String gamemodeKey = "";

        TranslatableComponent translatableComponent = event.getComponent();
        if (translatableComponent.args().isEmpty()) {
            Gamemode gamemode = new Gamemode(gamemodeKey, target);
            return Optional.of(gamemode);
        }

        if (translatableComponent.args().get(0) instanceof TranslatableComponent gamemodeComponent) {
            gamemodeKey = gamemodeComponent.key();
        } else if (translatableComponent.args().size() > 1
                && translatableComponent.args().get(0) instanceof TextComponent playerComponent
                && translatableComponent.args().get(1) instanceof TranslatableComponent gamemodeComponent) {
            target = playerComponent.content();
            gamemodeKey = gamemodeComponent.key();
        }

        Gamemode gamemode = new Gamemode(gamemodeKey, target);
        return Optional.of(gamemode);
    }

}
