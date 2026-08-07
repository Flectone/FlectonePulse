package net.flectone.pulse.platform.formatter;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.setting.MessageChannelSetting;
import net.flectone.pulse.constant.MessageFlag;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.model.event.IntegrationMessageFormat;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.event.message.context.VanishMessageContext;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.serializer.ComponentSerializer;
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

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class IntegrationFormatterImpl implements IntegrationFormatter {

    private static final Pattern FINAL_CLEAR_MESSAGE_PATTERN = Pattern.compile("[\\p{C}\\p{So}\\x{E0100}-\\x{E01EF}]+");

    private final MessagePipeline messagePipeline;
    private final ComponentSerializer componentSerializer;

    @Override
    public boolean isVanished(MessageContext messageContext) {
        return messageContext instanceof VanishMessageContext vanishMessageContext && vanishMessageContext.fakeMessage() && vanishMessageContext.vanished();
    }

    @NonNull
    @Override
    public List<String> getExistedMessageNames(@NonNull ModuleName moduleName, @NonNull IntegrationMessageFormat integrationMessageFormat, MessageChannelSetting messageChannelSetting) {
        Predicate<String> existChannelPredicate = string -> !messageChannelSetting.messageChannel().getOrDefault(string, List.of()).isEmpty();

        Stream<String> existedStream = integrationMessageFormat.messageNames().stream()
                .filter(existChannelPredicate);

        Stream<String> moduleStream = existChannelPredicate.test(moduleName.name())
                ? Stream.of(moduleName.name())
                : Stream.empty();

        return Stream.concat(existedStream, moduleStream).toList();
    }

    @NonNull
    @Override
    public UnaryOperator<String> createFormat(@NonNull IntegrationMessageFormat integrationMessageFormat, @NonNull MessageContext messageContext) {
        MessageFlag[] integrationFlags = new MessageFlag[]{MessageFlag.TRANSLATE_MODULE, MessageFlag.OBJECT_SPRITE_PROCESSING, MessageFlag.OBJECT_PLAYER_HEAD_PROCESSING, MessageFlag.INTERACTIVE_CHAT_COMPAT, MessageFlag.URL_PROCESSING};
        boolean[] integrationFlagsValues = new boolean[]{false, false, false, false, false};

        String finalMessage = plainSerialize(messagePipeline.build(messageContext.addFlags(integrationFlags, integrationFlagsValues)));
        String finalClearMessage = clearMessage(finalMessage);

        return string -> {
            String input = integrationMessageFormat.format().apply(string);
            if (StringUtils.isBlank(input)) return StringUtils.EMPTY;

            String format = plainSerialize(messagePipeline.build(messageContext.toBuilder()
                    .message(input)
                    .flags(integrationFlags, integrationFlagsValues)
                    .build()
            ));

            return StringUtils.replaceEach(
                    format,
                    new String[]{"<player>", "<final_message>", "<final_clear_message>"},
                    new String[]{messageContext.sender().name(), finalMessage, finalClearMessage}
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
