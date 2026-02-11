package net.flectone.pulse.platform.sender;

import com.github.retrooper.packetevents.protocol.chat.ChatType;
import com.github.retrooper.packetevents.protocol.chat.ChatTypes;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessageLegacy;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage_v1_16;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChatMessage;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSystemChatMessage;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;

import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MinecraftMessageSender implements MessageSender {

    private final PacketSender packetSender;
    private final PacketProvider packetProvider;
    private final IntegrationModule integrationModule;
    private final FLogger fLogger;


    @Override
    public void sendToConsole(String message) {
        fLogger.info(message);
    }

    @Override
    public void sendMessage(FPlayer fPlayer, Component component, boolean silent) {
        if (fPlayer.isConsole()) {
            sendToConsole(component);
            return;
        }

        // integration with InteractiveChat
        if (integrationModule.sendMessageWithInteractiveChat(fPlayer, component)) return;

        User user = packetProvider.getUser(fPlayer);
        if (user == null) return;

        // PacketEvents realization
        ClientVersion version = user.getPacketVersion();
        PacketWrapper<?> chatPacket;
        if (version.isNewerThanOrEquals(ClientVersion.V_1_19)) {
            chatPacket = new WrapperPlayServerSystemChatMessage(false, component);
        } else {
            ChatType type = ChatTypes.CHAT;
            ChatMessage message = version.isNewerThanOrEquals(ClientVersion.V_1_16)
                    ? new ChatMessage_v1_16(component, type, new UUID(0L, 0L))
                    : new ChatMessageLegacy(component, type);

            chatPacket = new WrapperPlayServerChatMessage(message);
        }

        packetSender.send(fPlayer.uuid(), chatPacket, silent);
    }

}
