package net.flectone.pulse.module.message.deop.listener;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.listener.AbstractPacketListener;
import net.flectone.pulse.module.message.deop.DeopModule;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;

@Singleton
public class DeopPacketListener extends AbstractPacketListener {

    private final DeopModule deopModule;

    @Inject
    public DeopPacketListener(DeopModule deopModule) {
        this.deopModule = deopModule;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.isCancelled()) return;

        TranslatableComponent translatableComponent = getTranslatableComponent(event);
        if (translatableComponent == null) return;

        String key = translatableComponent.key();
        if (cancelMessageNotDelivered(event, key)) return;
        if (!key.startsWith("commands.deop.success")) return;
        if (translatableComponent.args().isEmpty()) return;
        if (!deopModule.isEnable()) return;
        if (!(translatableComponent.args().get(0) instanceof TextComponent targetComponent)) return;

        event.setCancelled(true);

        deopModule.send(event.getUser().getUUID(), targetComponent.content());
    }
}
