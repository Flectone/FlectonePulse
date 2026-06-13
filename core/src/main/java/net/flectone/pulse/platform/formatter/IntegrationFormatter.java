package net.flectone.pulse.platform.formatter;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.setting.MessageChannelSetting;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.IntegrationMetadata;
import net.flectone.pulse.model.event.VanishMetadata;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.processing.serializer.ComponentSerializer;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.ModuleName;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.translation.GlobalTranslator;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Create format for external integrations (Discord, Telegram, Twitch, etc.)
 *
 * @author TheFaser
 * @since 1.10.0
 */
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class IntegrationFormatter {

    private static final Pattern FINAL_CLEAR_MESSAGE_PATTERN = Pattern.compile("[\\p{C}\\p{So}\\x{E0100}-\\x{E01EF}]+");

    private final MessagePipeline messagePipeline;
    private final ComponentSerializer componentSerializer;

    /**
     * Checks if the event sender is in vanish
     *
     * @param eventMetadata The event metadata containing sender information and optional vanish metadata
     * @return true if the sender is vanished and vanish should not be ignored, false otherwise
     */
    public boolean isVanished(EventMetadata<?> eventMetadata) {
        return eventMetadata instanceof VanishMetadata<?> vanishMetadata && vanishMetadata.fakeMessage() && vanishMetadata.vanished();
    }

    /**
     * Retrieves a list of message names that have corresponding non-empty channel configurations.
     *
     * @param moduleName The module name to check for existence in message channels
     * @param integrationMetadata Metadata containing the collection of message names to validate
     * @param messageChannelSetting Configuration providing the mapping of channel names to message lists
     * @return A list of message names that have non-empty channel configurations, including the module name if applicable
     */
    @NonNull
    public List<String> getExistedMessageNames(@NonNull ModuleName moduleName, @NonNull IntegrationMetadata integrationMetadata, MessageChannelSetting messageChannelSetting) {
        Predicate<String> existChannelPredicate = string -> !messageChannelSetting.messageChannel().getOrDefault(string, List.of()).isEmpty();

        Stream<String> existedStream = integrationMetadata.messageNames().stream()
                .filter(existChannelPredicate);

        Stream<String> moduleStream = existChannelPredicate.test(moduleName.name())
                ? Stream.of(moduleName.name())
                : Stream.empty();

        return Stream.concat(existedStream, moduleStream).toList();
    }

    /**
     * Creates a format function that processes and replaces placeholders in message templates.
     *
     * @param eventMetadata The event metadata containing sender information, message content, and flags
     * @param integrationMetadata Metadata providing integration-specific format transformations
     * @param format The format template string containing placeholders to be replaced
     * @return A unary operator that takes an input string and returns the formatted message with all placeholders resolved
     */
    @NonNull
    public UnaryOperator<String> createFormat(@NonNull EventMetadata<?> eventMetadata, @NonNull IntegrationMetadata integrationMetadata, @NonNull String format) {
        String plainFormat = plainSerialize(buildFormatComponent(format, eventMetadata));
        String plainMessage = plainSerialize(buildMessageComponent(eventMetadata));

        String finalMessage = Strings.CS.replace(
                plainFormat,
                "<message>",
                plainMessage
        );

        return string -> {
            String input = integrationMetadata.format().apply(string);
            if (StringUtils.isBlank(input)) return StringUtils.EMPTY;

            return StringUtils.replaceEach(
                    plainSerialize(buildFormatComponent(input, eventMetadata)),
                    new String[]{"<player>", "<message>", "<final_message>", "<final_clear_message>"},
                    new String[]{eventMetadata.sender().name(), plainMessage, finalMessage, clearMessage(finalMessage)}
            );
        };
    }

    private Component buildFormatComponent(String text, EventMetadata<?> eventMetadata) {
        MessageContext.MessageContextBuilder messageContextBuilder = MessageContext.builder()
                .sender(eventMetadata.sender())
                .receiver(FPlayer.UNKNOWN)
                .message(text)
                .flags(eventMetadata.flags())
                .flags(
                        new MessageFlag[]{MessageFlag.TRANSLATE_MODULE, MessageFlag.OBJECT_SPRITE_PROCESSING, MessageFlag.OBJECT_PLAYER_HEAD_PROCESSING, MessageFlag.INTERACTIVE_CHAT_COMPAT},
                        new boolean[]{false, false, false, false}
                );

        TagResolver[] tagResolvers = eventMetadata.resolveTags(FPlayer.UNKNOWN);
        if (tagResolvers != null) {
            messageContextBuilder = messageContextBuilder.tagResolvers(tagResolvers);
        }

        return messagePipeline.build(messageContextBuilder.build());
    }

    private Component buildMessageComponent(EventMetadata<?> eventMetadata) {
        String message = eventMetadata.message();
        if (StringUtils.isEmpty(message)) return Component.empty();

        return messagePipeline.build(MessageContext.builder()
                .sender(eventMetadata.sender())
                .receiver(FPlayer.UNKNOWN)
                .message(message)
                .flags(eventMetadata.flags())
                .flags(
                        new MessageFlag[]{MessageFlag.PLAYER_MESSAGE, MessageFlag.TRANSLATE_MODULE, MessageFlag.MENTION_MODULE, MessageFlag.INTERACTIVE_CHAT_COMPAT, MessageFlag.QUESTIONANSWER_MODULE, MessageFlag.URL_PROCESSING},
                        new boolean[]{true, false, false, false, false, false}
                )
                .build()
        );
    }

    private String clearMessage(String finalMessage) {
        return RegExUtils.replaceAll(
                (CharSequence) finalMessage,
                FINAL_CLEAR_MESSAGE_PATTERN,
                StringUtils.EMPTY
        );
    }

    private String plainSerialize(Component component) {
        return componentSerializer.toPlain(GlobalTranslator.render(component, Locale.ROOT));
    }

}
