package net.flectone.pulse.module.message.seed.listener;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSystemChatMessage;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.listener.AbstractPacketListener;
import net.flectone.pulse.module.message.seed.SeedModule;
import net.kyori.adventure.text.Component;
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
        if (event.getPacketType() != PacketType.Play.Server.SYSTEM_CHAT_MESSAGE) return;

        WrapperPlayServerSystemChatMessage wrapper = new WrapperPlayServerSystemChatMessage(event);
        Component component = wrapper.getMessage();
        if (!(component instanceof TranslatableComponent translatableComponent)) return;

        String key = translatableComponent.key();
        if (cancelMessageNotDelivered(event, key)) return;
        if (!key.startsWith("commands.seed.success")) return;
        if (translatableComponent.args().isEmpty()) return;
        if (!seedModule.isEnable()) return;
        if (!(translatableComponent.args().get(0) instanceof TranslatableComponent chatComponent)) return;
        if (chatComponent.args().isEmpty()) return;
        if (!(chatComponent.args().get(0) instanceof TextComponent seedComponent)) return;

        event.setCancelled(true);

        seedModule.send(event.getUser().getUUID(), seedComponent.content());
    }
}
