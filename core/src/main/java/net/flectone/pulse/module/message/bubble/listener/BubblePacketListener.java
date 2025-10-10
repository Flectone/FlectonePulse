package net.flectone.pulse.module.message.bubble.listener;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatMessage;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.message.bubble.BubbleModule;
import net.flectone.pulse.module.message.chat.ChatModule;
import net.flectone.pulse.service.FPlayerService;

import java.util.Collections;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BubblePacketListener implements PacketListener {

    private final FPlayerService fPlayerService;
    private final BubbleModule bubbleModule;
    private final ChatModule chatModule;

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.CHAT_MESSAGE) return;
        if (chatModule.isEnable() || !bubbleModule.isEnable()) return;

        FPlayer fPlayer = fPlayerService.getFPlayer(event.getUser().getUUID());

        WrapperPlayClientChatMessage wrapper = new WrapperPlayClientChatMessage(event);
        String message = wrapper.getMessage();

        bubbleModule.add(fPlayer, message, Collections.emptyList());
    }
}
