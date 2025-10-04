package net.flectone.pulse.platform.sender;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.chat.model.ChatMetadata;
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
public class IntegrationSender {

    private final Pattern finalClearMessagePattern = Pattern.compile("[\\p{C}\\p{So}\\x{E0100}-\\x{E01EF}]+");
    private final PlainTextComponentSerializer plainTextComponentSerializer = PlainTextComponentSerializer.plainText();

    private final IntegrationModule integrationModule;
    private final MessagePipeline messagePipeline;

    @Inject
    public IntegrationSender(IntegrationModule integrationModule,
                             MessagePipeline messagePipeline) {
        this.integrationModule = integrationModule;
        this.messagePipeline = messagePipeline;
    }

    @Async(independent = true)
    public void send(MessageType messageType, String format, EventMetadata<?> eventMetadata) {
        UnaryOperator<String> integrationOperator = eventMetadata.getIntegration();
        if (integrationOperator == null) return;
        if (!integrationModule.hasMessenger()) return;

        FEntity sender = eventMetadata.getSender();

        Component componentFormat = messagePipeline.builder(sender, FPlayer.UNKNOWN, format)
                .flag(MessageFlag.SENDER_COLOR_OUT, eventMetadata.isSenderColorOut())
                .flag(MessageFlag.TRANSLATE, false)
                .tagResolvers(eventMetadata.getTagResolvers(FPlayer.UNKNOWN))
                .build();

        String message = eventMetadata.getMessage();
        Component componentMessage = StringUtils.isEmpty(message)
                ? Component.empty()
                : messagePipeline
                .builder(sender, FPlayer.UNKNOWN, message)
                .flag(MessageFlag.SENDER_COLOR_OUT, eventMetadata.isSenderColorOut())
                .flag(MessageFlag.TRANSLATE, false)
                .flag(MessageFlag.USER_MESSAGE, true)
                .flag(MessageFlag.MENTION, false)
                .flag(MessageFlag.INTERACTIVE_CHAT, false)
                .flag(MessageFlag.QUESTION, false)
                .build();

        String plainFormat = plainSerialize(componentFormat);
        String plainMessage = plainSerialize(componentMessage);

        String finalMessage = Strings.CS.replace(
                plainFormat,
                "<message>",
                plainMessage
        );

        String finalClearMessage = RegExUtils.replaceAll(
                (CharSequence) finalMessage,
                finalClearMessagePattern,
                StringUtils.EMPTY
        );

        UnaryOperator<String> interfaceReplaceString = s -> {
            String input = integrationOperator.apply(s);
            if (StringUtils.isBlank(input)) return StringUtils.EMPTY;

            return StringUtils.replaceEach(
                    input,
                    new String[]{"<player>", "<message>", "<plain_message>", "<final_message>", "<final_clear_message>"},
                    new String[]{sender.getName(), message, plainMessage,  finalMessage, finalClearMessage}
            );
        };

        String messageName = createMessageName(messageType, eventMetadata);
        integrationModule.sendMessage(sender, messageName, interfaceReplaceString);
    }

    private String plainSerialize(Component component) {
        return plainTextComponentSerializer.serialize(GlobalTranslator.render(component, Locale.ROOT));
    }

    private String createMessageName(MessageType messageType, EventMetadata<?> eventMetadata) {
        if (messageType != MessageType.CHAT) return messageType.name();
        if (!(eventMetadata instanceof ChatMetadata<?> chatMetadata)) return "UNKNOWN";

        return messageType.name() + "_" + chatMetadata.getChatName().toUpperCase();
    }

}
