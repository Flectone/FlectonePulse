package net.flectone.pulse.module.message.op.listener;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.listener.AbstractPacketListener;
import net.flectone.pulse.module.message.op.OpModule;
import net.flectone.pulse.util.MinecraftTranslationKeys;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;

@Singleton
public class OpPacketListener extends AbstractPacketListener {

    private final OpModule opModule;

    @Inject
    public OpPacketListener(OpModule opModule) {
        this.opModule = opModule;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.isCancelled()) return;

        TranslatableComponent translatableComponent = getTranslatableComponent(event);
        if (translatableComponent == null) return;

        MinecraftTranslationKeys key = MinecraftTranslationKeys.fromString(translatableComponent.key());
        if (cancelMessageNotDelivered(event, key)) return;
        if (key != MinecraftTranslationKeys.COMMANDS_OP_SUCCESS) return;
        if (translatableComponent.args().isEmpty()) return;
        if (!opModule.isEnable()) return;
        if (!(translatableComponent.args().get(0) instanceof TextComponent targetComponent)) return;

        event.setCancelled(true);
        opModule.send(event.getUser().getUUID(), targetComponent.content());
    }
}
