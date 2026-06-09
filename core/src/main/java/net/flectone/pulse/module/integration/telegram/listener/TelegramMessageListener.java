package net.flectone.pulse.module.integration.telegram.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.module.integration.telegram.TelegramModule;
import net.flectone.pulse.module.integration.telegram.execution.dispatcher.TelegramCommandDispatcher;
import net.flectone.pulse.module.integration.telegram.extractor.TelegramChatIdExtractor;
import net.flectone.pulse.module.integration.telegram.model.TelegramClient;
import net.flectone.pulse.module.integration.telegram.provider.TelegramClientProvider;
import net.flectone.pulse.module.integration.telegram.sender.TelegramSender;
import net.flectone.pulse.util.logging.FLogger;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TelegramMessageListener implements TelegramEventListener {

    private final TelegramModule telegramModule;
    private final TelegramChatIdExtractor telegramChatIdExtractor;
    private final TelegramCommandDispatcher telegramCommandDispatcher;
    private final TelegramClientProvider telegramClientProvider;
    private final TelegramSender telegramSender;
    private final TaskScheduler taskScheduler;
    private final FLogger fLogger;

    @Override
    public void consume(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            taskScheduler.runAsync(() -> handleMessage(message));
        }
    }

    public void handleMessage(@NonNull Message message) {
        TelegramClient telegramClient = telegramClientProvider.get();
        if (telegramClient == null) return;

        // delete telegram bot notification
        if (isNewChatNameMessage(message)) {
            String chatId = telegramChatIdExtractor.extract(message);
            if (telegramModule.localization().infoChannel().containsKey(chatId)) {
                try {
                    telegramClient.executeMethod(DeleteMessage.builder()
                            .chatId(chatId)
                            .messageId(message.getMessageId())
                            .build()
                    );
                } catch (TelegramApiException e) {
                    fLogger.warning(e);
                }
            }
        }

        if (telegramCommandDispatcher.executeCommand(message)) return;

        String text = message.getText();
        if (text == null) return;

        User author = message.getFrom();
        if (author == null) return;

        // always ignore ourselves
        if (author.getIsBot() && (telegramModule.config().ignoreAllBots() || author.getId().equals(telegramClient.id()))) return;

        String chat = message.getChat().getTitle();
        if (chat == null) return;

        Pair<String, String> reply = null;
        if (isRealReply(message)) {
            Message replied = message.getReplyToMessage();
            User user = replied.getFrom();
            if (user != null) {
                reply = Pair.of(user.getUserName(), replied.getText());
            }
        }

        String chatId = telegramChatIdExtractor.extract(message);
        List<String> chats = telegramModule.config().messageChannel().get(telegramModule.name().name());
        if (chats == null || !chats.contains(chatId)) return;

        telegramSender.sendMessage(author, chat, chatId, text, reply);
    }

    private boolean isRealReply(@NonNull Message message) {
        if (message.getReplyToMessage() == null) {
            return false;
        }

        Message replied = message.getReplyToMessage();

        boolean hasContent = replied.hasText()
                || replied.hasPhoto()
                || replied.hasDocument()
                || replied.hasVideo()
                || replied.getAudio() != null
                || replied.getVoice() != null
                || replied.getSticker() != null;

        boolean isNotTopicCreation = replied.getForumTopicCreated() == null
                && replied.getForumTopicEdited() == null
                && replied.getForumTopicClosed() == null
                && replied.getForumTopicReopened() == null;

        return hasContent && isNotTopicCreation;
    }

    private boolean isNewChatNameMessage(@NonNull Message message) {
        if (message.getNewChatTitle() == null && message.getForumTopicEdited() == null) return false;

        User user = message.getFrom();
        return user != null && user.getIsBot();
    }

}
