package net.flectone.pulse.platform.sender;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.chat.model.Chat;
import net.flectone.pulse.module.message.chat.model.ChatMetadata;
import net.flectone.pulse.module.message.vanilla.model.VanillaMetadata;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.MessageType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.util.Locale;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

/**
 * Sends formatted messages to external integrations (Discord, Telegram, Twitch, etc.)
 *
 * <p><b>Usage example:</b>
 * <pre>{@code
 * IntegrationSender integrationSender = flectonePulse.get(IntegrationSender.class);
 *
 * // Send chat message to external integrations
 * integrationSender.asyncSend(MessageType.CHAT, "<final_message>", eventMetadata);
 * }</pre>
 *
 * @since 1.5.0
 * @author TheFaser
 */
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class IntegrationSender {

    private static final Pattern FINAL_CLEAR_MESSAGE_PATTERN = Pattern.compile("[\\p{C}\\p{So}\\x{E0100}-\\x{E01EF}]+");
    private static final Map<MessageFlag, Boolean> DEFAULT_MESSAGE_FLAGS = Map.of(
            MessageFlag.TRANSLATE, false,
            MessageFlag.USER_MESSAGE, true,
            MessageFlag.MENTION, false,
            MessageFlag.INTERACTIVE_CHAT, false,
            MessageFlag.QUESTION, false
    );

    private final IntegrationModule integrationModule;
    private final MessagePipeline messagePipeline;
    private final TaskScheduler taskScheduler;

    /**
     * Sends a message to integrations asynchronously.
     *
     * @param messageType the type of message being sent
     * @param format the message format string
     * @param eventMetadata the event metadata containing sender and message
     */
    public void asyncSend(MessageType messageType, String format, EventMetadata<?> eventMetadata) {
        taskScheduler.runAsync(() -> send(messageType, format, eventMetadata), true);
    }

    /**
     * Sends a message to integrations
     *
     * @param messageType the type of message being sent
     * @param format the message format string
     * @param eventMetadata the event metadata containing sender and message
     */
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
        MessageContext messageContext = messagePipeline.createContext(sender, FPlayer.UNKNOWN, text)
                .withFlag(MessageFlag.SENDER_COLOR_OUT, eventMetadata.isSenderColorOut())
                .withFlag(MessageFlag.TRANSLATE, false)
                .addTagResolvers(eventMetadata.getTagResolvers(FPlayer.UNKNOWN));

        return messagePipeline.build(messageContext);
    }

    private Component createMessage(EventMetadata<?> eventMetadata) {
        String message = eventMetadata.getMessage();
        if (StringUtils.isEmpty(message)) return Component.empty();

        MessageContext context = messagePipeline.createContext(eventMetadata.getSender(), FPlayer.UNKNOWN, message)
                .withFlags(DEFAULT_MESSAGE_FLAGS)
                .withFlag(MessageFlag.SENDER_COLOR_OUT, eventMetadata.isSenderColorOut());

        return messagePipeline.build(context);
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
