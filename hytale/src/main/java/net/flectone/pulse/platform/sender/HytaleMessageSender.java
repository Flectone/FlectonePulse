package net.flectone.pulse.platform.sender;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.logging.FLogger;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.serializer.HytaleComponentSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;

import java.util.Locale;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class HytaleMessageSender implements MessageSender {

    private final FLogger fLogger;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final HytaleComponentSerializer componentSerializer;

    @Override
    public void sendToConsole(String message) {
        fLogger.info(message);
    }

    @Override
    public void sendToConsole(Component component) {
        sendToConsole(ANSIComponentSerializer.ansi().serialize(GlobalTranslator.render(component, Locale.ROOT)));
    }

    @Override
    public void sendMessage(FPlayer fPlayer, Component component, boolean silent) {
        if (fPlayer.isConsole()) {
            sendToConsole(component);
            return;
        }

        Object player = platformPlayerAdapter.convertToPlatformPlayer(fPlayer);
        if (player == null) return;

        PlayerRef playerRef = (PlayerRef) player;
        playerRef.sendMessage(componentSerializer.toHytale(component));
    }

}
