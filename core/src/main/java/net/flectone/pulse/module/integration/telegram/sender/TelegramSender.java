package net.flectone.pulse.module.integration.telegram.sender;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.IntegrationMetadata;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.integration.telegram.TelegramModule;
import net.flectone.pulse.module.integration.telegram.extractor.TelegramChatIdExtractor;
import net.flectone.pulse.module.integration.telegram.model.TelegramClient;
import net.flectone.pulse.module.integration.telegram.model.TelegramMetadata;
import net.flectone.pulse.module.integration.telegram.provider.TelegramClientProvider;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.function.UnaryOperator;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TelegramSender {

    private final TelegramModule telegramModule;
    private final TelegramChatIdExtractor telegramChatIdExtractor;
    private final TelegramClientProvider telegramClientProvider;
    private final MessageDispatcher messageDispatcher;
    private final MessagePipeline messagePipeline;
    private final FLogger fLogger;

    public void sendMessage(@NonNull Message message,
                            @NonNull String text) {
        TelegramClient telegramClient = telegramClientProvider.get();
        if (telegramClient == null) return;

        String chatId = telegramChatIdExtractor.extract(message);
        SendMessage.SendMessageBuilder<?, ?> sendMessage = SendMessage
                .builder()
                .chatId(chatId)
                .text(Strings.CS.replace(text, "<id>", chatId));

        if (message.isTopicMessage()) {
            sendMessage = sendMessage.messageThreadId(message.getMessageThreadId());
        }

        try {
            telegramClient.executeMethod(sendMessage.build());
        } catch (TelegramApiException e) {
            fLogger.warning(e);
        }
    }

    public void sendMessage(@NonNull FEntity sender,
                            @NonNull String messageName,
                            @NonNull UnaryOperator<String> telegramString) {
        TelegramClient telegramClient = telegramClientProvider.get();
        if (telegramClient == null) return;

        List<String> channels = telegramModule.config().messageChannel().get(messageName);
        if (channels == null) return;
        if (channels.isEmpty()) return;

        Localization.Integration.Telegram localization = telegramModule.localization();
        String message = localization.messageChannel().getOrDefault(messageName, "<final_message>");
        if (StringUtils.isEmpty(message)) return;

        message = telegramString.apply(message);
        if (StringUtils.isEmpty(message)) return;

        for (String chat : channels) {

            SendMessage.SendMessageBuilder<?, ?> sendMessageBuilder = SendMessage.builder()
                    .chatId(chat)
                    .text(message);

            if (chat.contains("_")) {
                sendMessageBuilder
                        .messageThreadId(Integer.parseInt(chat.split("_")[1]));
            }

            SendMessage sendMessage = sendMessageBuilder.build();

            switch (telegramModule.config().parseMode()) {
                case MARKDOWN -> sendMessage.enableMarkdown(true);
                case MARKDOWN_V2 -> sendMessage.enableMarkdownV2(true);
                case HTML -> sendMessage.enableHtml(true);
            }

            try {
                telegramClient.executeMethod(sendMessage);
            } catch (TelegramApiException e) {
                fLogger.warning(e);
            }
        }
    }

    public void sendMessage(@NonNull User user,
                            @NonNull String chat,
                            @NonNull String chatId,
                            @NonNull String message,
                            @Nullable Pair<String, String> reply) {
        TelegramClient telegramClient = telegramClientProvider.get();
        if (telegramClient == null) return;

        String userName = StringUtils.defaultString(user.getUserName());
        String firstName = user.getFirstName();
        String lastName = StringUtils.defaultString(user.getLastName());

        messageDispatcher.dispatch(telegramModule, TelegramMetadata.<Localization.Integration.Telegram>builder()
                .base(EventMetadata.<Localization.Integration.Telegram>builder()
                        .sender(telegramClient.sender())
                        .format(localization -> StringUtils.replaceEach(
                                StringUtils.defaultString(localization.messageChannel().get(telegramModule.name().name())),
                                new String[]{"<name>", "<user_name>", "<first_name>", "<last_name>", "<chat>"},
                                new String[]{userName, userName, firstName, lastName, chat}
                        ))
                        .message(message)
                        .range(Range.get(Range.Type.PROXY))
                        .destination(telegramModule.config().destination())
                        .sound(telegramModule.soundOrThrow())
                        .tagResolvers(fResolver -> new TagResolver[]{messagePipeline.resolver("reply", (_, _) -> {
                            if (reply == null) return MessagePipeline.ReplacementTag.emptyTag();

                            return Tag.inserting(messagePipeline.build(MessageContext.builder()
                                    .message(telegramModule.localization(fResolver).formatReply())
                                    .tagResolvers(
                                            messagePipeline.resolver("reply_user", Tag.preProcessParsed(StringUtils.defaultString(reply.getLeft()))),
                                            messagePipeline.resolver("reply_message", (_, _) -> Tag.selfClosingInserting(messagePipeline.build(MessageContext.builder()
                                                    .sender(telegramClient.sender())
                                                    .receiver(fResolver)
                                                    .message(reply.getRight())
                                                    .flag(MessageFlag.PLAYER_MESSAGE, true)
                                                    .build()
                                            )))
                                    )
                                    .build()
                            ));
                        })})
                        .integration(IntegrationMetadata.builder()
                                .format(string -> StringUtils.replaceEach(
                                        string,
                                        new String[]{"<name>", "<user_name>", "<first_name>", "<last_name>", "<chat>"},
                                        new String[]{userName, userName, firstName, lastName, StringUtils.defaultString(chat)}
                                ))
                                .messageNames(List.of(telegramModule.name().name() + "_" + chatId, telegramModule.name().name() + "_" + chat.toUpperCase()))
                                .build()
                        )
                        .build()
                )
                .userName(userName)
                .firstName(firstName)
                .lastName(lastName)
                .chat(chat)
                .build()
        );
    }

}
