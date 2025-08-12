package net.flectone.pulse.module.message.chat.listener;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatMessage;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.message.chat.ChatModule;
import net.flectone.pulse.service.FPlayerService;

@Singleton
public class ChatPacketListener implements PacketListener {

    private final FPlayerService fPlayerService;
    private final ChatModule chatModule;

    @Inject
    public ChatPacketListener(FPlayerService fPlayerService,
                              ChatModule chatModule) {
        this.fPlayerService = fPlayerService;
        this.chatModule = chatModule;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.CHAT_MESSAGE) return;
        if (!chatModule.isEnable()) return;

        FPlayer fPlayer = fPlayerService.getFPlayer(event.getUser().getUUID());
        if (chatModule.isModuleDisabledFor(fPlayer)) return;

        WrapperPlayClientChatMessage wrapper = new WrapperPlayClientChatMessage(event);
        String message = wrapper.getMessage();

        event.setCancelled(true);
        asyncSend(fPlayer, message);
    }

    @Async
    public void asyncSend(FPlayer fPlayer, String message) {
        chatModule.send(fPlayer, message, () -> {}, (string, value) -> {});
    }
}
