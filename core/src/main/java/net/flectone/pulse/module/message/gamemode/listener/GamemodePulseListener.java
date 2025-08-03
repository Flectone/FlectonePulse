package net.flectone.pulse.module.message.gamemode.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.TranslatableMessageReceiveEvent;
import net.flectone.pulse.module.message.gamemode.GamemodeModule;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;

@Singleton
public class GamemodePulseListener implements PulseListener {

    private final GamemodeModule gamemodeModule;

    @Inject
    public GamemodePulseListener(GamemodeModule gamemodeModule) {
        this.gamemodeModule = gamemodeModule;
    }

    @Pulse
    public void onTranslatableMessageReceiveEvent(TranslatableMessageReceiveEvent event) {
        MinecraftTranslationKey key = event.getKey();
        if (!key.startsWith("commands.gamemode.success") && key != MinecraftTranslationKey.GAMEMODE_CHANGED) return;

        String target = event.getFPlayer().getName();
        String gamemodeKey = "";

        TranslatableComponent translatableComponent = event.getComponent();
        if (translatableComponent.args().isEmpty()) {
            event.setCancelled(true);
            gamemodeModule.send(event.getFPlayer(), gamemodeKey, target);
            return;
        }

        if (translatableComponent.args().get(0) instanceof TranslatableComponent gamemodeComponent) {
            gamemodeKey = gamemodeComponent.key();
        } else if (translatableComponent.args().size() > 1
                && translatableComponent.args().get(0) instanceof TextComponent playerComponent
                && translatableComponent.args().get(1) instanceof TranslatableComponent gamemodeComponent) {
            target = playerComponent.content();
            gamemodeKey = gamemodeComponent.key();
        }

        event.setCancelled(true);
        gamemodeModule.send(event.getFPlayer(), gamemodeKey, target);
    }

}
