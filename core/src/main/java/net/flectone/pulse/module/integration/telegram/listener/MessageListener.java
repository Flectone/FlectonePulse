package net.flectone.pulse.module.integration.telegram.listener;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.integration.telegram.TelegramIntegration;
import net.flectone.pulse.module.integration.telegram.model.TelegramMetadata;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.List;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MessageListener extends EventListener {

    private final FileResolver fileResolver;
    private final Provider<TelegramIntegration> telegramIntegration;

    @Override
    public void onEnable() {}

    @Override
    public Integration.Telegram config() {
        return fileResolver.getIntegration().getTelegram();
    }

    @Override
    public Permission.Integration.Telegram permission() {
        return fileResolver.getPermission().getIntegration().getTelegram();
    }

    @Override
    public Localization.Integration.Telegram localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getIntegration().getTelegram();
    }

    @Override
    public void consume(Update update) {
        if (!update.hasMessage()) return;

        Message message = update.getMessage();

        // delete telegram bot notification
        if (isNewChatNameMessage(message)) {
            String chatId = getChatId(message);
            if (localization().getInfoChannel().containsKey(chatId)) {
                deleteMessage(chatId, message.getMessageId());
            }
        }

        String text = message.getText();
        if (text == null) return;

        User author = message.getFrom();
        if (author == null) return;

        String chat = message.getChat().getTitle();
        if (chat == null) return;

        String chatID = getChatId(message);

        if (text.equalsIgnoreCase("/id")) {
            sendInfoMessage(chatID, message);
            return;
        }

        List<String> chats = config().getMessageChannel().get(MessageType.FROM_TELEGRAM_TO_MINECRAFT.name());
        if (chats == null || !chats.contains(chatID)) return;

        sendMessage(author, chat, text);
    }

    @Async
    public void sendMessage(User user, String chat, String message) {
        String userName = StringUtils.defaultString(user.getUserName());
        String firstName = user.getFirstName();
        String lastName = StringUtils.defaultString(user.getLastName());

        sendMessage(TelegramMetadata.<Localization.Integration.Telegram>builder()
                .sender(FPlayer.UNKNOWN)
                .format(localization -> StringUtils.replaceEach(
                        localization.getForMinecraft(),
                        new String[]{"<name>", "<user_name>", "<first_name>", "<last_name>", "<chat>"},
                        new String[]{userName, userName, firstName, lastName, StringUtils.defaultString(chat)}
                ))
                .userName(userName)
                .firstName(firstName)
                .lastName(lastName)
                .chat(chat)
                .message(message)
                .range(Range.get(Range.Type.PROXY))
                .destination(config().getDestination())
                .sound(getModuleSound())
                .integration(string -> StringUtils.replaceEach(
                        string,
                        new String[]{"<name>", "<user_name>", "<first_name>", "<last_name>", "<chat>"},
                        new String[]{userName, userName, firstName, lastName, StringUtils.defaultString(chat)}
                ))
                .build()
        );
    }

    private boolean isNewChatNameMessage(Message message) {
        if (message.getNewChatTitle() == null && message.getForumTopicEdited() == null) return false;

        User user = message.getFrom();
        return user != null && user.getIsBot();
    }

    private String getChatId(Message message) {
        return message.getChatId() + (message.isTopicMessage() ? "_" + message.getMessageThreadId() : "");
    }

    private void deleteMessage(String chatId, Integer messageId) {
        telegramIntegration.get().executeMethod(DeleteMessage.builder()
                .chatId(chatId)
                .messageId(messageId)
                .build()
        );
    }

    private void sendInfoMessage(String chatID, Message message) {
        SendMessage.SendMessageBuilder<?, ?> sendMessage = SendMessage
                .builder()
                .chatId(chatID)
                .text("Channel id: " + chatID);

        if (message.isTopicMessage()) {
            sendMessage = sendMessage.messageThreadId(message.getMessageThreadId());
        }

        telegramIntegration.get().executeMethod(sendMessage.build());
    }
}
