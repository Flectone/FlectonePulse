package net.flectone.pulse.module.message.bubble.listener;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatMessage;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.message.bubble.BubbleModule;
import net.flectone.pulse.module.message.chat.ChatModule;
import net.flectone.pulse.service.FPlayerService;

@Singleton
public class BubbleListener implements PacketListener {

    private final FPlayerService fPlayerService;
    private final BubbleModule bubbleModule;
    private final ChatModule chatModule;

    @Inject
    public BubbleListener(FPlayerService fPlayerService,
                          BubbleModule bubbleModule,
                          ChatModule chatModule) {
        this.fPlayerService = fPlayerService;
        this.bubbleModule = bubbleModule;
        this.chatModule = chatModule;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.CHAT_MESSAGE) return;
        if (chatModule.isEnable() || !bubbleModule.isEnable()) return;

        FPlayer fPlayer = fPlayerService.getFPlayer(event.getUser().getUUID());

        WrapperPlayClientChatMessage wrapper = new WrapperPlayClientChatMessage(event);
        String message = wrapper.getMessage();

        bubbleModule.add(fPlayer, message);
    }
}
