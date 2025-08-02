package net.flectone.pulse.module.message.chat.listener;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatMessage;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.message.chat.ChatModule;
import net.flectone.pulse.service.FPlayerService;

@Singleton
public class ChatListener implements PacketListener {

    private final FPlayerService fPlayerService;
    private final ChatModule chatModule;

    @Inject
    public ChatListener(FPlayerService fPlayerService,
                        ChatModule chatModule) {
        this.fPlayerService = fPlayerService;
        this.chatModule = chatModule;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.CHAT_MESSAGE) return;
        if (!chatModule.isEnable()) return;

        FPlayer fPlayer = fPlayerService.getFPlayer(event.getUser().getUUID());

        WrapperPlayClientChatMessage wrapper = new WrapperPlayClientChatMessage(event);
        String message = wrapper.getMessage();

        chatModule.send(fPlayer,
                message,
                () -> event.setCancelled(true),
                (string, aBoolean) -> event.setCancelled(true)
        );
    }
}
