package net.flectone.pulse.module.integration.telegram.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TelegramChatIdExtractor {

    @NonNull
    public String extract(@NonNull Message message) {
        return message.getChatId() + (message.isTopicMessage() ? "_" + message.getMessageThreadId() : "");
    }

}
