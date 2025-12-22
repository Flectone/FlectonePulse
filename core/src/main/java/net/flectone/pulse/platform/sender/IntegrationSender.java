package net.flectone.pulse.platform.sender;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.chat.model.Chat;
import net.flectone.pulse.module.message.chat.model.ChatMetadata;
import net.flectone.pulse.module.message.vanilla.model.VanillaMetadata;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.MessageType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.util.Locale;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class IntegrationSender {

    private static final Pattern FINAL_CLEAR_MESSAGE_PATTERN = Pattern.compile("[\\p{C}\\p{So}\\x{E0100}-\\x{E01EF}]+");

    private final IntegrationModule integrationModule;
    private final MessagePipeline messagePipeline;

    @Async(independent = true)
    public void asyncSend(MessageType messageType, String format, EventMetadata<?> eventMetadata) {
        send(messageType, format, eventMetadata);
    }

    public void send(MessageType messageType, String format, EventMetadata<?> eventMetadata) {
        UnaryOperator<String> integrationOperator = eventMetadata.getIntegration();
        if (integrationOperator == null) return;
        if (!integrationModule.hasMessenger()) return;

        FEntity sender = eventMetadata.getSender();

        String plainFormat = plainSerialize(createFormat(format, eventMetadata));
        String plainMessage = plainSerialize(createMessage(eventMetadata));

        String finalMessage = Strings.CS.replace(
                plainFormat,
                "<message>",
                plainMessage
        );

        UnaryOperator<String> interfaceReplaceString = s -> {
            String input = integrationOperator.apply(s);
            if (StringUtils.isBlank(input)) return StringUtils.EMPTY;

            return StringUtils.replaceEach(
                    plainSerialize(createFormat(input, eventMetadata)),
                    new String[]{"<player>", "<message>", "<plain_message>", "<final_message>", "<final_clear_message>"},
                    new String[]{sender.getName(), eventMetadata.getMessage(), plainMessage,  finalMessage, clearMessage(finalMessage)}
            );
        };

        String messageName = createMessageName(messageType, eventMetadata);
        integrationModule.sendMessage(sender, messageName, interfaceReplaceString);
    }

    private Component createFormat(String text, EventMetadata<?> eventMetadata) {
        FEntity sender = eventMetadata.getSender();
        return messagePipeline.builder(sender, FPlayer.UNKNOWN, text)
                .flag(MessageFlag.SENDER_COLOR_OUT, eventMetadata.isSenderColorOut())
                .flag(MessageFlag.TRANSLATE, false)
                .tagResolvers(eventMetadata.getTagResolvers(FPlayer.UNKNOWN))
                .build();
    }

    private Component createMessage(EventMetadata<?> eventMetadata) {
        String message = eventMetadata.getMessage();
        FEntity sender = eventMetadata.getSender();
        return StringUtils.isEmpty(message)
                ? Component.empty()
                : messagePipeline.builder(sender, FPlayer.UNKNOWN, message)
                .flag(MessageFlag.SENDER_COLOR_OUT, eventMetadata.isSenderColorOut())
                .flag(MessageFlag.TRANSLATE, false)
                .flag(MessageFlag.USER_MESSAGE, true)
                .flag(MessageFlag.MENTION, false)
                .flag(MessageFlag.INTERACTIVE_CHAT, false)
                .flag(MessageFlag.QUESTION, false)
                .build();
    }

    private String clearMessage(String finalMessage) {
        return RegExUtils.replaceAll(
                (CharSequence) finalMessage,
                FINAL_CLEAR_MESSAGE_PATTERN,
                StringUtils.EMPTY
        );
    }

    private String plainSerialize(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(GlobalTranslator.render(component, Locale.ROOT));
    }

    private String createMessageName(MessageType messageType, EventMetadata<?> eventMetadata) {
        return switch (messageType) {
            case CHAT -> {
                if (!(eventMetadata instanceof ChatMetadata<?> chatMetadata)) yield "UNKNOWN";

                Chat chat = chatMetadata.getChat();
                if (chat.name() == null) yield messageType.name();

                yield messageType.name() + "_" + chat.name().toUpperCase();
            }
            case VANILLA -> {
                if (!(eventMetadata instanceof VanillaMetadata<?> vanillaMetadata)) yield "UNKNOWN";

                String vanillaMessageName = vanillaMetadata.getParsedComponent().vanillaMessage().name();
                if (vanillaMessageName.isEmpty()) yield messageType.name();

                yield vanillaMessageName.toUpperCase();
            }
            default -> messageType.name();
        };
    }

}
