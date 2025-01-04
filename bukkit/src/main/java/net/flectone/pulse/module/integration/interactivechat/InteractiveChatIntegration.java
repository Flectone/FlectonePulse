package net.flectone.pulse.module.integration.interactivechat;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.loohp.interactivechat.api.InteractiveChatAPI;
import com.loohp.interactivechat.listeners.ChatEvents;
import com.loohp.interactivechat.registry.Registry;
import net.flectone.pulse.logger.FLogger;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.module.integration.FIntegration;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.NotNull;

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

    public String checkMention(AsyncPlayerChatEvent event) {
        return ChatEvents.checkMention(event);
    }

    @NotNull
    public String mark(FEntity sender, String message) {
        StringBuilder stringBuilder = new StringBuilder();
        for(String string : message.split(" ")) {
            if (!Registry.MENTION_TAG_CONVERTER.containsTags(string) && !string.contains("<cmd=") && !string.contains("<chat=")) {
                string = InteractiveChatAPI.markSender(string, sender.getUuid());
            }

            stringBuilder.append(string).append(" ");
        }

        return stringBuilder.toString().trim();
    }
}
