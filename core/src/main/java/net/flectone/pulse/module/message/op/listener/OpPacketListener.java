package net.flectone.pulse.module.message.op.listener;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSystemChatMessage;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.listener.AbstractPacketListener;
import net.flectone.pulse.module.message.op.OpModule;
import net.kyori.adventure.text.Component;
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
        if (event.getPacketType() != PacketType.Play.Server.SYSTEM_CHAT_MESSAGE) return;

        WrapperPlayServerSystemChatMessage wrapper = new WrapperPlayServerSystemChatMessage(event);
        Component component = wrapper.getMessage();
        if (!(component instanceof TranslatableComponent translatableComponent)) return;

        String key = translatableComponent.key();
        if (cancelMessageNotDelivered(event, key)) return;
        if (!key.startsWith("commands.op.success")) return;
        if (translatableComponent.args().isEmpty()) return;
        if (!opModule.isEnable()) return;
        if (!(translatableComponent.args().get(0) instanceof TextComponent targetComponent)) return;

        event.setCancelled(true);

        opModule.send(event.getUser().getUUID(), targetComponent.content());
    }
}
