package net.flectone.pulse.module.message.bubble.listener;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatMessage;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPassengers;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.message.bubble.BubbleModule;
import net.flectone.pulse.module.message.bubble.render.MinecraftBubbleRender;
import net.flectone.pulse.module.message.chat.ChatModule;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.service.FPlayerService;

import java.util.Collections;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BubblePacketListener implements PacketListener {

    private final FPlayerService fPlayerService;
    private final BubbleModule bubbleModule;
    private final MinecraftBubbleRender bubbleRenderer;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final ModuleController moduleController;
    private final ChatModule chatModule;

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() != PacketType.Play.Server.SET_PASSENGERS) return;
        if (!moduleController.isEnable(bubbleModule)) return;

        WrapperPlayServerSetPassengers wrapper = new WrapperPlayServerSetPassengers(event);
        UUID playerUUID = platformPlayerAdapter.getPlayerByEntityId(wrapper.getEntityId());
        if (playerUUID == null) return;

        bubbleRenderer.removeBubbleIf(bubble -> bubble.getSender().uuid().equals(playerUUID));
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.CHAT_MESSAGE) return;
        if (moduleController.isEnable(chatModule) || !moduleController.isEnable(bubbleModule)) return;

        FPlayer fPlayer = fPlayerService.getFPlayer(event.getUser().getUUID());

        WrapperPlayClientChatMessage wrapper = new WrapperPlayClientChatMessage(event);
        String message = wrapper.getMessage();

        bubbleModule.add(fPlayer, message, Collections.emptyList());
    }
}
