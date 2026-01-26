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
import net.flectone.pulse.module.message.chat.model.ChatMetadata;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.MessageType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
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
        UnaryOperator<String> integrationOperator = eventMetadata.integration();
        if (integrationOperator == null) return;
        if (!integrationModule.hasMessenger()) return;

        FEntity sender = eventMetadata.sender();

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
                    new String[]{"<player>", "<message>", "<final_message>", "<final_clear_message>"},
                    new String[]{sender.getName(), plainMessage,  finalMessage, clearMessage(finalMessage)}
            );
        };

        for (String specificMessageName : createSpecificMessageNames(messageType, eventMetadata)) {
            integrationModule.sendMessage(sender, specificMessageName, interfaceReplaceString);
        }

        integrationModule.sendMessage(sender, messageType.name(), interfaceReplaceString);
    }

    private Component createFormat(String text, EventMetadata<?> eventMetadata) {
        FEntity sender = eventMetadata.sender();
        MessageContext context = messagePipeline.createContext(sender, FPlayer.UNKNOWN, text)
                .withFlags(eventMetadata.flags())
                .addFlags(
                        new MessageFlag[]{MessageFlag.TRANSLATE, MessageFlag.OBJECT_SPRITE, MessageFlag.OBJECT_PLAYER_HEAD},
                        new boolean[]{false, false, false}
                )
                .addTagResolvers(eventMetadata.resolveTags(FPlayer.UNKNOWN));

        return messagePipeline.build(context);
    }

    private Component createMessage(EventMetadata<?> eventMetadata) {
        String message = eventMetadata.message();
        if (StringUtils.isEmpty(message)) return Component.empty();

        MessageContext context = messagePipeline.createContext(eventMetadata.sender(), FPlayer.UNKNOWN, message)
                .withFlags(eventMetadata.flags())
                .addFlags(
                        new MessageFlag[]{MessageFlag.USER_MESSAGE, MessageFlag.TRANSLATE, MessageFlag.MENTION, MessageFlag.INTERACTIVE_CHAT, MessageFlag.QUESTION},
                        new boolean[]{true, false, false, false, false}
                );

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

    protected Collection<String> createSpecificMessageNames(MessageType messageType, EventMetadata<?> eventMetadata) {
        if (messageType == MessageType.CHAT
                && eventMetadata instanceof ChatMetadata<?> chatMetadata
                && chatMetadata.chat().name() != null) {
            return List.of((messageType.name() + "_" + chatMetadata.chat().name()).toUpperCase());
        }

        return Collections.emptyList();
    }

}
