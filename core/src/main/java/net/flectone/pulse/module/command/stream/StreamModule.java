package net.flectone.pulse.module.command.stream;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.stream.listener.StreamPulseListener;
import net.flectone.pulse.module.command.stream.model.StreamMetadata;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.SettingText;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.incendo.cloud.suggestion.Suggestion;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class StreamModule extends AbstractModuleCommand<Localization.Command.Stream> implements PulseListener {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final CommandParserProvider commandParserProvider;
    private final ListenerRegistry listenerRegistry;

    @Override
    public void onEnable() {
        super.onEnable();

        String promptType = addPrompt(0, Localization.Command.Prompt::type);
        String promptUrl = addPrompt(1, Localization.Command.Prompt::url);
        registerCommand(manager -> manager
                .permission(permission().name())
                .required(promptType, commandParserProvider.singleMessageParser(), typeSuggestion())
                .optional(promptUrl, commandParserProvider.nativeMessageParser())
        );

        listenerRegistry.register(StreamPulseListener.class);
    }

    private @NonNull BlockingSuggestionProvider<FPlayer> typeSuggestion() {
        return (context, input) -> List.of(
                Suggestion.suggestion("start"),
                Suggestion.suggestion("end")
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer, true)) return;

        String type = getArgument(commandContext, 0);
        Boolean needStart = switch (type) {
            case "start" -> true;
            case "end" -> false;
            default -> null;
        };

        if (needStart == null) return;

        boolean isStream = localization().prefixTrue().equals(fPlayer.getSetting(SettingText.STREAM_PREFIX));

        if (isStream && needStart && !fPlayer.isUnknown()) {
            sendErrorMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Stream::already)
                    .build()
            );

            return;
        }

        if (!isStream && !needStart) {
            sendErrorMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Stream::not)
                    .build()
            );

            return;
        }

        setStreamPrefix(fPlayer, needStart
                ? localization().prefixTrue()
                : StringUtils.isEmpty(localization().prefixFalse()) ? null : localization().prefixFalse()
        );

        if (needStart) {
            String promptUrl = getPrompt(1);
            Optional<String> optionalUrl = commandContext.optional(promptUrl);
            String rawString = optionalUrl.orElse("");

            String urls = Arrays.stream(rawString.split("\\s+"))
                    .filter(this::isUrl)
                    .collect(Collectors.joining(" "));

            sendMessage(StreamMetadata.<Localization.Command.Stream>builder()
                    .sender(fPlayer)
                    .format(replaceUrls(urls))
                    .turned(true)
                    .urls(urls)
                    .range(config().range())
                    .destination(config().destination())
                    .sound(soundOrThrow())
                    .proxy(dataOutputStream -> dataOutputStream.writeString(urls))
                    .integration(string -> Strings.CS.replace(string, "<urls>", StringUtils.defaultString(urls)))
                    .build()
            );
        } else {
            sendMessage(StreamMetadata.<Localization.Command.Stream>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Stream::formatEnd)
                    .turned(false)
                    .destination(config().destination())
                    .build()
            );
        }
    }

    public void setStreamPrefix(FPlayer fPlayer, String prefix) {
        fPlayer.setSetting(SettingText.STREAM_PREFIX, prefix);
        fPlayerService.saveOrUpdateSetting(fPlayer, SettingText.STREAM_PREFIX);
    }

    @Override
    public MessageType messageType() {
        return MessageType.COMMAND_STREAM;
    }

    @Override
    public Command.Stream config() {
        return fileFacade.command().stream();
    }

    @Override
    public Permission.Command.Stream permission() {
        return fileFacade.permission().command().stream();
    }

    @Override
    public Localization.Command.Stream localization(FEntity sender) {
        return fileFacade.localization(sender).command().stream();
    }

    public MessageContext addTag(MessageContext messageContext) {
        if (messageContext.isFlag(MessageFlag.USER_MESSAGE)) return messageContext;
        if (!messageContext.message().contains(MessagePipeline.ReplacementTag.STREAM_PREFIX.getTagName())) return messageContext;

        FEntity sender = messageContext.sender();
        if (!(sender instanceof FPlayer fPlayer)) return messageContext;
        if (isModuleDisabledFor(fPlayer)) return messageContext;

        return messageContext.addTagResolver(MessagePipeline.ReplacementTag.STREAM_PREFIX, (argumentQueue, context) -> {
            String streamPrefix = fPlayer.getSetting(SettingText.STREAM_PREFIX);
            if (StringUtils.isEmpty(streamPrefix)) return Tag.selfClosingInserting(Component.empty());

            return Tag.preProcessParsed(streamPrefix);
        });
    }

    public Function<Localization.Command.Stream, String> replaceUrls(String string) {
        return message -> {
            List<String> urls = Arrays.stream(string.split(" "))
                    .map(url -> Strings.CS.replace(message.urlTemplate(), "<url>", url))
                    .toList();

            return Strings.CS.replace(message.formatStart(), "<urls>", String.join("<br>", urls));
        };
    }

    private boolean isUrl(String string) {
        try {
            new URI(string).toURL();
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }
}
