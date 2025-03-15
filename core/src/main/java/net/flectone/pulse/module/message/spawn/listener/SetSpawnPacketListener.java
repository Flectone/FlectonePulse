package net.flectone.pulse.module.message.spawn.listener;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.listener.AbstractPacketListener;
import net.flectone.pulse.module.message.spawn.SpawnModule;
import net.flectone.pulse.util.MinecraftTranslationKeys;
import net.kyori.adventure.text.TranslatableComponent;

@Singleton
public class SetSpawnPacketListener extends AbstractPacketListener {

    private final SpawnModule spawnModule;

    @Inject
    public SetSpawnPacketListener(SpawnModule spawnModule) {
        this.spawnModule = spawnModule;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.isCancelled()) return;
        if (!spawnModule.isEnable()) return;

        TranslatableComponent translatableComponent = getTranslatableComponent(event);
        if (translatableComponent == null) return;

        MinecraftTranslationKeys key = MinecraftTranslationKeys.fromString(translatableComponent.key());
        if (cancelMessageNotDelivered(event, key)) return;
        if (key != MinecraftTranslationKeys.BLOCK_MINECRAFT_SET_SPAWN) return;

        event.setCancelled(true);
        spawnModule.send(event.getUser().getUUID(), key);
    }
}

