package net.flectone.pulse.module.message.quit.listener;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.listener.AbstractPacketListener;
import net.flectone.pulse.module.message.quit.QuitModule;
import net.flectone.pulse.util.MinecraftTranslationKeys;
import net.kyori.adventure.text.TranslatableComponent;

@Singleton
public class QuitPacketListener extends AbstractPacketListener {

    private final QuitModule quitModule;

    @Inject
    public QuitPacketListener(QuitModule quitModule) {
        this.quitModule = quitModule;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.isCancelled()) return;

        TranslatableComponent translatableComponent = getTranslatableComponent(event);
        if (translatableComponent == null) return;

        MinecraftTranslationKeys key = MinecraftTranslationKeys.fromString(translatableComponent.key());
        if (cancelMessageNotDelivered(event, key)) return;
        if (key != MinecraftTranslationKeys.MULTIPLAYER_PLAYER_LEFT) return;
        if (!quitModule.isEnable()) return;

        event.setCancelled(true);
    }
}
