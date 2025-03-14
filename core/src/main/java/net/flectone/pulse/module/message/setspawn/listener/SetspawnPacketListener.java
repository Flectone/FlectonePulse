package net.flectone.pulse.module.message.setspawn.listener;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.listener.AbstractPacketListener;
import net.flectone.pulse.module.message.setspawn.SetspawnModule;
import net.kyori.adventure.text.TranslatableComponent;

@Singleton
public class SetspawnPacketListener extends AbstractPacketListener {

    private final SetspawnModule setspawnModule;

    @Inject
    public SetspawnPacketListener(SetspawnModule setspawnModule) {
        this.setspawnModule = setspawnModule;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.isCancelled()) return;

        TranslatableComponent translatableComponent = getTranslatableComponent(event);
        if (translatableComponent == null) return;

        String key = translatableComponent.key();
        if (cancelMessageNotDelivered(event, key)) return;
        if (!key.startsWith("block.minecraft.set_spawn")) return;
        if (!setspawnModule.isEnable()) return;

        event.setCancelled(true);
        setspawnModule.send(event.getUser().getUUID());
    }
}

