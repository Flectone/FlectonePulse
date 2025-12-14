package net.flectone.pulse.module.integration.telegram.listener;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.integration.telegram.TelegramIntegration;
import net.flectone.pulse.module.integration.telegram.model.TelegramMetadata;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.MessageType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.incendo.cloud.type.tuple.Pair;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MessageListener extends EventListener {

    private final FileResolver fileResolver;
    private final Provider<TelegramIntegration> telegramIntegration;
    private final FPlayerService fPlayerService;
    private final MessagePipeline messagePipeline;

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

        if (executeCommand(message)) return;

        String text = message.getText();
        if (text == null) return;

        User author = message.getFrom();
        if (author == null) return;

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

        List<String> chats = config().getMessageChannel().get(MessageType.FROM_TELEGRAM_TO_MINECRAFT.name());
        if (chats == null || !chats.contains(getChatId(message))) return;

        sendMessage(author, chat, text, reply);
    }

    @Async
    public void sendMessage(User user, String chat, String message, Pair<String, String> reply) {
        String userName = StringUtils.defaultString(user.getUserName());
        String firstName = user.getFirstName();
        String lastName = StringUtils.defaultString(user.getLastName());

        sendMessage(TelegramMetadata.<Localization.Integration.Telegram>builder()
                .sender(FPlayer.UNKNOWN)
                .format(localization -> StringUtils.replaceEach(
                        StringUtils.defaultString(localization.getMessageChannel().get(MessageType.FROM_TELEGRAM_TO_MINECRAFT.name())),
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
                .tagResolvers(fResolver -> new TagResolver[]{TagResolver.resolver("reply", (argumentQueue, context) -> {
                    if (reply == null) return Tag.selfClosingInserting(Component.empty());

                    Component componentReply = messagePipeline.builder(localization().getFormatReply())
                            .tagResolvers(
                                    TagResolver.resolver("reply_user", Tag.preProcessParsed(StringUtils.defaultString(reply.first()))),
                                    TagResolver.resolver("reply_message", Tag.preProcessParsed(StringUtils.defaultString(reply.second())))
                            )
                            .build();

                    return Tag.inserting(componentReply);
                })})
                .integration(string -> StringUtils.replaceEach(
                        string,
                        new String[]{"<name>", "<user_name>", "<first_name>", "<last_name>", "<chat>"},
                        new String[]{userName, userName, firstName, lastName, StringUtils.defaultString(chat)}
                ))
                .build()
        );
    }

    private boolean executeCommand(Message message) {
        String text = message.getText();
        if (StringUtils.isEmpty(text)) return false;

        String[] parts = text.toLowerCase().trim().split(" ", 2);
        String commandName = parts[0];
        String arguments = parts.length > 1 ? parts[1] : "";

        for (Map.Entry<String, Integration.Command> commandEntry : config().getCustomCommand().entrySet()) {
            Integration.Command command = commandEntry.getValue();
            if (!command.getAliases().contains(commandName)) continue;

            FPlayer fPlayer = FPlayer.UNKNOWN;

            if (command.isNeedPlayer()) {
                if (arguments.isEmpty()) {
                    sendMessageToTelegram(message, buildMessage(fPlayer, Localization.Integration.Telegram::getNullPlayer));
                    return true;
                }

                String playerName = arguments.split(" ")[0];
                fPlayer = fPlayerService.getFPlayer(playerName);
                if (fPlayer.isUnknown()) {
                    sendMessageToTelegram(message, buildMessage(fPlayer, Localization.Integration.Telegram::getNullPlayer));
                    return true;
                }
            }

            String localizationString = localization().getCustomCommand().get(commandEntry.getKey());
            if (StringUtils.isEmpty(localizationString)) return true;

            String formattedMessage = buildMessage(fPlayer, localizationString);
            sendMessageToTelegram(message, formattedMessage);
            return true;
        }

        return false;
    }

    private boolean isRealReply(Message message) {
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

    private boolean isNewChatNameMessage(Message message) {
        if (message.getNewChatTitle() == null && message.getForumTopicEdited() == null) return false;

        User user = message.getFrom();
        return user != null && user.getIsBot();
    }

    private String buildMessage(FPlayer fPlayer, Function<Localization.Integration.Telegram, String> stringFunction) {
        return buildMessage(fPlayer, stringFunction.apply(localization()));
    }

    private String buildMessage(FPlayer fPlayer, String localization) {
        return messagePipeline.builder(fPlayer, localization)
                .flag(MessageFlag.OBJECT_PLAYER_HEAD, false)
                .flag(MessageFlag.OBJECT_SPRITE, false)
                .plainSerializerBuild();
    }

    public String getChatId(Message message) {
        return message.getChatId() + (message.isTopicMessage() ? "_" + message.getMessageThreadId() : "");
    }

    public void deleteMessage(String chatId, Integer messageId) {
        telegramIntegration.get().executeMethod(DeleteMessage.builder()
                .chatId(chatId)
                .messageId(messageId)
                .build()
        );
    }

    public void sendMessageToTelegram(Message message, String text) {
        String chatId = getChatId(message);
        SendMessage.SendMessageBuilder<?, ?> sendMessage = SendMessage
                .builder()
                .chatId(chatId)
                .text(Strings.CS.replace(text, "<id>", chatId));

        if (message.isTopicMessage()) {
            sendMessage = sendMessage.messageThreadId(message.getMessageThreadId());
        }

        telegramIntegration.get().executeMethod(sendMessage.build());
    }

}
