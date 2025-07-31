package net.flectone.pulse.module.message.quit.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.constant.MinecraftTranslationKey;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.event.message.TranslatableMessageReceiveEvent;
import net.flectone.pulse.model.event.player.PlayerQuitEvent;
import net.flectone.pulse.module.message.quit.QuitModule;

@Singleton
public class QuitPulseListener implements PulseListener {

    private final QuitModule quitModule;

    @Inject
    public QuitPulseListener(QuitModule quitModule) {
        this.quitModule = quitModule;
    }

    @Pulse
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        FPlayer fPlayer = event.getPlayer();
        quitModule.send(fPlayer);
    }

    @Pulse
    public void onTranslatableMessageReceive(TranslatableMessageReceiveEvent event) {
        if (event.getKey() != MinecraftTranslationKey.MULTIPLAYER_PLAYER_LEFT) return;

        event.cancelPacket();
    }

}
