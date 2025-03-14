package net.flectone.pulse.module.message.clear.listener;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.listener.AbstractPacketListener;
import net.flectone.pulse.module.message.clear.ClearModule;
import net.flectone.pulse.util.MinecraftTranslationKeys;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;

@Singleton
public class ClearPacketListener extends AbstractPacketListener {

    private final ClearModule clearModule;

    @Inject
    public ClearPacketListener(ClearModule clearModule) {
        this.clearModule = clearModule;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.isCancelled()) return;

        TranslatableComponent translatableComponent = getTranslatableComponent(event);
        if (translatableComponent == null) return;

        MinecraftTranslationKeys key = MinecraftTranslationKeys.fromString(translatableComponent.key());
        if (cancelMessageNotDelivered(event, key)) return;
        if (!key.startsWith("commands.clear.success")) return;
        if (translatableComponent.args().size() < 2) return;
        if (!clearModule.isEnable()) return;

        if (!(translatableComponent.args().get(0) instanceof TextComponent numberComponent)) return;
        String number = numberComponent.content();

        if (!(translatableComponent.args().get(1) instanceof TextComponent targetComponent)) return;
        String value = targetComponent.content();

        event.setCancelled(true);
        clearModule.send(event.getUser().getUUID(), key, number, value);
    }
}
