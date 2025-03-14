package net.flectone.pulse.module.message.bed.listener;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.listener.AbstractPacketListener;
import net.flectone.pulse.module.message.bed.BedModule;
import net.flectone.pulse.util.MinecraftTranslationKeys;
import net.kyori.adventure.text.TranslatableComponent;

@Singleton
public class BedPacketListener extends AbstractPacketListener {

    private final BedModule bedModule;

    @Inject
    public BedPacketListener(BedModule bedModule) {
        this.bedModule = bedModule;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.isCancelled()) return;

        TranslatableComponent translatableComponent = getTranslatableComponent(event);
        if (translatableComponent == null) return;

        MinecraftTranslationKeys key = MinecraftTranslationKeys.fromString(translatableComponent.key());
        if (cancelMessageNotDelivered(event, key)) return;
        if (!key.startsWith("block.minecraft.bed.")) return;
        if (!bedModule.isEnable()) return;

        event.setCancelled(true);
        bedModule.send(event.getUser().getUUID(), key);
    }
}
