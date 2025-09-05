package net.flectone.pulse.module.message.gamemode.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.gamemode.model.Gamemode;
import net.flectone.pulse.processing.extractor.Extractor;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Optional;

@Singleton
public class GamemodeExtractor extends Extractor {

    @Inject
    public GamemodeExtractor() {
    }

    public Optional<Gamemode> extract(MessageReceiveEvent event) {
        String target = event.getFPlayer().getName();
        String gamemodeKey = "";

        TranslatableComponent translatableComponent = event.getTranslatableComponent();
        if (translatableComponent.arguments().isEmpty()) {
            Gamemode gamemode = new Gamemode(gamemodeKey, target);
            return Optional.of(gamemode);
        }

        if (translatableComponent.arguments().getFirst().asComponent() instanceof TranslatableComponent gamemodeComponent) {
            gamemodeKey = gamemodeComponent.key();
        } else if (translatableComponent.arguments().size() > 1
                && translatableComponent.arguments().get(0).asComponent() instanceof TextComponent playerComponent
                && translatableComponent.arguments().get(1).asComponent() instanceof TranslatableComponent gamemodeComponent) {
            target = extractTarget(playerComponent);
            gamemodeKey = gamemodeComponent.key();
        }

        Gamemode gamemode = new Gamemode(gamemodeKey, target);
        return Optional.of(gamemode);
    }

}
