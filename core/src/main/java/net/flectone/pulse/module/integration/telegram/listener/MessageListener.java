package net.flectone.pulse.module.integration.telegram.listener;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.module.integration.telegram.model.TelegramMetadata;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.integration.telegram.TelegramIntegration;
import net.flectone.pulse.processing.resolver.FileResolver;
import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.List;

@Singleton
public class MessageListener extends EventListener {

    private final Integration.Telegram integration;
    private final Provider<TelegramIntegration> telegramIntegration;

    @Inject
    public MessageListener(FileResolver fileResolver,
                           Provider<TelegramIntegration> telegramIntegration) {
        this.integration = fileResolver.getIntegration().getTelegram();
        this.telegramIntegration = telegramIntegration;
    }

    @Override
    public void onEnable() {}

    @Override
    protected boolean isConfigEnable() {
        return integration.isEnable();
    }

    @Override
    public void consume(Update update) {
        if (!update.hasMessage()) return;

        Message message = update.getMessage();

        String text = message.getText();
        if (text == null) return;

        String author = message.getFrom().getUserName();
        if (author == null) return;

        String chat = message.getChat().getTitle();
        if (chat == null) return;

        String chatID = String.valueOf(message.getChatId());

        if (message.isTopicMessage()) {
            chatID += "_" + message.getMessageThreadId();
        }

        if (text.equalsIgnoreCase("/id")) {
            sendInfoMessage(chatID, message);
            return;
        }

        List<String> chats = integration.getMessageChannel().get(MessageType.FROM_TELEGRAM_TO_MINECRAFT.name());
        if (chats == null || !chats.contains(chatID)) return;

        sendMessage(author, chat, text);
    }

    @Async
    public void sendMessage(String author, String chat, String message) {
        sendMessage(TelegramMetadata.<Localization.Integration.Telegram>builder()
                .sender(FPlayer.UNKNOWN)
                .format(s -> StringUtils.replaceEach(
                        s.getForMinecraft(),
                        new String[]{"<name>", "<chat>"},
                        new String[]{String.valueOf(author), String.valueOf(chat)}
                ))
                .author(author)
                .chat(chat)
                .message(message)
                .range(Range.get(Range.Type.PROXY))
                .destination(integration.getDestination())
                .sound(getModuleSound())
                .integration(string -> StringUtils.replaceEach(
                        string,
                        new String[]{"<name>", "<chat>"},
                        new String[]{author, chat}
                ))
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
