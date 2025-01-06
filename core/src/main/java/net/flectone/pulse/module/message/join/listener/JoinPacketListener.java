package net.flectone.pulse.module.message.join.listener;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.listener.AbstractPacketListener;
import net.flectone.pulse.module.message.join.JoinModule;
import net.kyori.adventure.text.TranslatableComponent;

@Singleton
public class JoinPacketListener extends AbstractPacketListener {

    private final JoinModule joinModule;

    @Inject
    public JoinPacketListener(JoinModule joinModule) {
        this.joinModule = joinModule;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.isCancelled()) return;

        TranslatableComponent translatableComponent = getTranslatableComponent(event);
        if (translatableComponent == null) return;

        String key = translatableComponent.key();
        if (cancelMessageNotDelivered(event, key)) return;
        if (!key.startsWith("multiplayer.player.joined")) return;
        if (!joinModule.isEnable()) return;

        event.setCancelled(true);
    }
}
