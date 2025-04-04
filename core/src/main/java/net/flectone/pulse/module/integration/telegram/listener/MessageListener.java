package net.flectone.pulse.module.integration.telegram.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.configuration.Integration;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.integration.telegram.TelegramIntegration;
import net.flectone.pulse.util.MessageTag;
import net.flectone.pulse.util.Range;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.List;

@Singleton
public class MessageListener extends EventListener {

    @Getter private final Integration.Telegram integration;

    private final TaskScheduler taskScheduler;

    @Inject private TelegramIntegration telegramIntegration;

    @Inject
    public MessageListener(FileManager fileManager,
                           TaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;

        integration = fileManager.getIntegration().getTelegram();
    }

    @Override
    public void reload() {}

    @Override
    public boolean isConfigEnable() {
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

        List<String> chats = integration.getMessageChannel().get(MessageTag.FROM_TELEGRAM_TO_MINECRAFT);
        if (!chats.contains(chatID)) return;

        taskScheduler.runAsync(() -> builder(FPlayer.UNKNOWN)
                .range(Range.PROXY)
                .destination(integration.getDestination())
                .filter(fPlayer -> fPlayer.isSetting(FPlayer.Setting.TELEGRAM))
                .tag(MessageTag.FROM_TELEGRAM_TO_MINECRAFT)
                .format(s -> s.getForMinecraft()
                        .replace("<name>", author)
                        .replace("<chat>", chat)
                )
                .message(text)
                .proxy(output -> {
                    output.writeUTF(author);
                    output.writeUTF(chat);
                    output.writeUTF(text);
                })
                .integration()
                .sound(getSound())
                .sendBuilt()
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

        telegramIntegration.executeMethod(sendMessage.build());
    }
}
