package net.flectone.pulse.module.integration.interactivechat;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.api.InteractiveChatAPI;
import com.loohp.interactivechat.data.PlayerDataManager;
import com.loohp.interactivechat.listeners.ChatEvents;
import com.loohp.interactivechat.registry.Registry;
import net.flectone.pulse.logger.FLogger;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.module.integration.FIntegration;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

// I personally don't want to support InteractiveChat
// but users really like it, so...

@Singleton
public class InteractiveChatIntegration implements FIntegration {

    private final FLogger fLogger;

    @Inject
    public InteractiveChatIntegration(FLogger fLogger) {
        this.fLogger = fLogger;
    }

    @Override
    public void hook() {
        fLogger.info("InteractiveChat hooked");
    }

    // from InteractiveChat
    // why isn't this in the InteractiveChatAPI?
    // why AsyncPlayerChatEvent?
    // ...
    public String checkMention(FEntity fSender, String message) {
        Player sender = Bukkit.getPlayer(fSender.getUuid());
        if (sender == null) return message;

        PlayerDataManager.PlayerData data = InteractiveChat.playerDataManager.getPlayerData(sender);
        if (InteractiveChat.allowMention && (data == null || !data.isMentionDisabled())) {
            String processedMessage;
            if (!InteractiveChat.disableEveryone && (processedMessage = ChatEvents.checkMentionEveryone("chat", message, sender)) != null) {
                return processedMessage;
            }

            if (!InteractiveChat.disableHere && (processedMessage = ChatEvents.checkMentionHere("chat", message, sender)) != null) {
                return processedMessage;
            }

            if ((processedMessage = ChatEvents.checkMentionPlayers("chat", message, sender)) != null) {
                return processedMessage;
            }
        }

        return message;
    }

    // https://github.com/LOOHP/InteractiveChat/issues/164
    public String markSender(FEntity fSender, String message) {
        UUID sender = fSender.getUuid();
        if (Bukkit.getPlayer(fSender.getUuid()) == null) return message;

        StringBuilder stringBuilder = new StringBuilder();
        for (String string : message.split(" ")) {
            if (!Registry.MENTION_TAG_CONVERTER.containsTags(string)
                    && !string.contains("<cmd=")
                    && !string.contains("<chat=")) {
                string = InteractiveChatAPI.markSender(string, sender);
            }

            stringBuilder
                    .append(string)
                    .append(" ");
        }

        return stringBuilder.toString().trim();
    }

    // InteractiveChat uses ProtocolLib, so it doesn't see messages from PacketEvents
    // when support PacketEvents?
    public boolean sendMessage(FEntity fReceiver, Component message) {
        Player receiver = Bukkit.getPlayer(fReceiver.getUuid());
        if (receiver == null) return false;

        try {
            String serializedMessage = MiniMessage.miniMessage().serialize(message);
            var deserializedMessage = com.loohp.interactivechat.libs.net.kyori.adventure.text.minimessage.MiniMessage
                    .miniMessage()
                    .deserialize(serializedMessage);

            InteractiveChatAPI.sendMessage(receiver, deserializedMessage);
        } catch (Exception e) {
            fLogger.warning(e);
            return false;
        }

        return true;
    }
}
