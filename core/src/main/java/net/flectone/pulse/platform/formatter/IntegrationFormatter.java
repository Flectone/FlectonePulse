package net.flectone.pulse.platform.formatter;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.setting.MessageChannelSetting;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.event.IntegrationMessageFormat;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.event.message.context.VanishMessageContext;
import net.flectone.pulse.processing.serializer.ComponentSerializer;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.ModuleName;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.translation.GlobalTranslator;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
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
     * @param messageContext The message context containing sender information and optional vanish state
     * @return true if the sender is vanished and vanish should not be ignored, false otherwise
     */
    public boolean isVanished(MessageContext messageContext) {
        return messageContext instanceof VanishMessageContext vanishMessageContext && vanishMessageContext.fakeMessage() && vanishMessageContext.vanished();
    }

    /**
     * Retrieves a list of message names that have corresponding non-empty channel configurations.
     *
     * @param moduleName The module name to check for existence in message channels
     * @param integrationMessageFormat Contains the collection of message names to validate
     * @param messageChannelSetting Configuration providing the mapping of channel names to message lists
     * @return A list of message names that have non-empty channel configurations, including the module name if applicable
     */
    @NonNull
    public List<String> getExistedMessageNames(@NonNull ModuleName moduleName, @NonNull IntegrationMessageFormat integrationMessageFormat, MessageChannelSetting messageChannelSetting) {
        Predicate<String> existChannelPredicate = string -> !messageChannelSetting.messageChannel().getOrDefault(string, List.of()).isEmpty();

        Stream<String> existedStream = integrationMessageFormat.messageNames().stream()
                .filter(existChannelPredicate);

        Stream<String> moduleStream = existChannelPredicate.test(moduleName.name())
                ? Stream.of(moduleName.name())
                : Stream.empty();

        return Stream.concat(existedStream, moduleStream).toList();
    }

    /**
     * Creates a format function that processes and replaces placeholders in message templates.
     *
     * @param integrationMessageFormat Provides integration-specific format transformations
     * @param messageContext The message context containing sender information and flags for message processing
     * @return A unary operator that takes an input string and returns the formatted message with all placeholders resolved
     */
    @NonNull
    public UnaryOperator<String> createFormat(@NonNull IntegrationMessageFormat integrationMessageFormat, @NonNull MessageContext messageContext) {
        return string -> {
            String input = integrationMessageFormat.format().apply(string);
            if (StringUtils.isBlank(input)) return StringUtils.EMPTY;

            String message = plainSerialize(messagePipeline.build(messageContext.addFlags(
                    new MessageFlag[]{MessageFlag.TRANSLATE_MODULE, MessageFlag.OBJECT_SPRITE_PROCESSING, MessageFlag.OBJECT_PLAYER_HEAD_PROCESSING, MessageFlag.INTERACTIVE_CHAT_COMPAT},
                    new boolean[]{false, false, false, false}
            )));

            return StringUtils.replaceEach(
                    message,
                    new String[]{"<player>", "<clear_message>"},
                    new String[]{messageContext.sender().name(), clearMessage(message)}
            );
        };
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
