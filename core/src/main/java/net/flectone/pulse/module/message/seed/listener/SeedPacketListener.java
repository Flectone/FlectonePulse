package net.flectone.pulse.module.message.seed.listener;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.listener.AbstractPacketListener;
import net.flectone.pulse.module.message.seed.SeedModule;
import net.flectone.pulse.util.MinecraftTranslationKeys;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;

@Singleton
public class SeedPacketListener extends AbstractPacketListener {

    private final SeedModule seedModule;

    @Inject
    public SeedPacketListener(SeedModule seedModule) {
        this.seedModule = seedModule;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.isCancelled()) return;

        TranslatableComponent translatableComponent = getTranslatableComponent(event);
        if (translatableComponent == null) return;

        MinecraftTranslationKeys key = MinecraftTranslationKeys.fromString(translatableComponent.key());
        if (cancelMessageNotDelivered(event, key)) return;
        if (key != MinecraftTranslationKeys.COMMANDS_SEED_SUCCESS) return;
        if (translatableComponent.args().isEmpty()) return;
        if (!seedModule.isEnable()) return;
        if (!(translatableComponent.args().get(0) instanceof TranslatableComponent chatComponent)) return;
        if (chatComponent.args().isEmpty()) return;
        if (!(chatComponent.args().get(0) instanceof TextComponent seedComponent)) return;

        event.setCancelled(true);

        seedModule.send(event.getUser().getUUID(), seedComponent.content());
    }
}
