package net.flectone.pulse.platform.sender;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.MessageType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

@Singleton
public class IntegrationSender {

    private final Pattern finalClearMessagePattern = Pattern.compile("[\\p{C}\\p{So}\\x{E0100}-\\x{E01EF}]+");

    private final IntegrationModule integrationModule;
    private final MessagePipeline messagePipeline;

    @Inject
    public IntegrationSender(IntegrationModule integrationModule,
                             MessagePipeline messagePipeline) {
        this.integrationModule = integrationModule;
        this.messagePipeline = messagePipeline;
    }

    @Async
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

        String messageContent = eventMetadata.getMessage();
        Component componentMessage = StringUtils.isEmpty(messageContent)
                ? Component.empty()
                : messagePipeline
                .builder(sender, FPlayer.UNKNOWN, messageContent)
                .flag(MessageFlag.SENDER_COLOR_OUT, eventMetadata.isSenderColorOut())
                .flag(MessageFlag.TRANSLATE, false)
                .flag(MessageFlag.USER_MESSAGE, true)
                .flag(MessageFlag.MENTION, false)
                .flag(MessageFlag.INTERACTIVE_CHAT, false)
                .flag(MessageFlag.QUESTION, false)
                .build();

        PlainTextComponentSerializer serializer = PlainTextComponentSerializer.plainText();
        String finalFormattedMessage = Strings.CS.replace(
                serializer.serialize(componentFormat),
                "<message>",
                serializer.serialize(componentMessage)
        );

        UnaryOperator<String> interfaceReplaceString = s -> {
            String input = integrationOperator.apply(s);
            if (StringUtils.isBlank(input)) return StringUtils.EMPTY;

            String clearMessage = RegExUtils.replaceAll((CharSequence) finalFormattedMessage, finalClearMessagePattern, StringUtils.EMPTY);
            return StringUtils.replaceEach(
                    input,
                    new String[]{"<player>", "<final_message>", "<final_clear_message>"},
                    new String[]{sender.getName(), finalFormattedMessage, clearMessage}
            );
        };

        integrationModule.sendMessage(sender, messageType, interfaceReplaceString);
    }

}
