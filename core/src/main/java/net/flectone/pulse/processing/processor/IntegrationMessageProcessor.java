package net.flectone.pulse.processing.processor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.setting.MessageChannelSetting;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.IntegrationMetadata;
import net.flectone.pulse.model.event.VanishMetadata;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.ModuleName;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
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

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class IntegrationMessageProcessor {

    private static final Pattern FINAL_CLEAR_MESSAGE_PATTERN = Pattern.compile("[\\p{C}\\p{So}\\x{E0100}-\\x{E01EF}]+");

    private final MessagePipeline messagePipeline;
    private final IntegrationModule integrationModule;

    public boolean isVanished(EventMetadata<?> eventMetadata) {
        FEntity sender = eventMetadata.sender();
        return eventMetadata instanceof VanishMetadata<?> vanishMetadata && !vanishMetadata.ignoreVanish() && integrationModule.isVanished(sender);
    }

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

    @NonNull
    public UnaryOperator<String> createFormatter(@NonNull EventMetadata<?> eventMetadata, @NonNull IntegrationMetadata integrationMetadata, @NonNull String format) {
        String plainFormat = plainSerialize(createFormat(format, eventMetadata));
        String plainMessage = plainSerialize(createMessage(eventMetadata));

        String finalMessage = Strings.CS.replace(
                plainFormat,
                "<message>",
                plainMessage
        );

        return string -> {
            String input = integrationMetadata.format().apply(string);
            if (StringUtils.isBlank(input)) return StringUtils.EMPTY;

            return StringUtils.replaceEach(
                    plainSerialize(createFormat(input, eventMetadata)),
                    new String[]{"<player>", "<message>", "<final_message>", "<final_clear_message>"},
                    new String[]{eventMetadata.sender().name(), plainMessage, finalMessage, clearMessage(finalMessage)}
            );
        };
    }

    private Component createFormat(String text, EventMetadata<?> eventMetadata) {
        FEntity sender = eventMetadata.sender();
        MessageContext context = messagePipeline.createContext(sender, FPlayer.UNKNOWN, text)
                .withFlags(eventMetadata.flags())
                .addFlags(
                        new MessageFlag[]{MessageFlag.TRANSLATE_MODULE, MessageFlag.OBJECT_SPRITE_PROCESSING, MessageFlag.OBJECT_PLAYER_HEAD_PROCESSING},
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
                        new MessageFlag[]{MessageFlag.PLAYER_MESSAGE, MessageFlag.TRANSLATE_MODULE, MessageFlag.MENTION_MODULE, MessageFlag.INTERACTIVE_CHAT_COMPAT, MessageFlag.QUESTIONANSWER_MODULE, MessageFlag.URL_PROCESSING},
                        new boolean[]{true, false, false, false, false, false}
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

}
