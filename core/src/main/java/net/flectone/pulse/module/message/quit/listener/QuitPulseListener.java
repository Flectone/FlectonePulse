package net.flectone.pulse.module.message.quit.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
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
        quitModule.send(fPlayer, false);
    }

    @Pulse
    public void onTranslatableMessageReceive(MessageReceiveEvent event) {
        if (event.getTranslationKey() != MinecraftTranslationKey.MULTIPLAYER_PLAYER_LEFT) return;

        event.setCancelled(true);
    }

}
