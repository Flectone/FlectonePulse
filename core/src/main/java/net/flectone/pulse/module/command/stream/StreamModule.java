package net.flectone.pulse.module.command.stream;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.localization.Localization;
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
import net.flectone.pulse.processing.resolver.FileResolver;
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

    private final FileResolver fileResolver;
    private final FPlayerService fPlayerService;
    private final CommandParserProvider commandParserProvider;
    private final ListenerRegistry listenerRegistry;

    @Override
    public void onEnable() {
        super.onEnable();

        String promptType = addPrompt(0, Localization.Command.Prompt::getType);
        String promptUrl = addPrompt(1, Localization.Command.Prompt::getUrl);
        registerCommand(manager -> manager
                .permission(permission().getName())
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

        boolean isStream = fPlayer.getSetting(SettingText.STREAM_PREFIX) != null;

        if (isStream && needStart && !fPlayer.isUnknown()) {
            sendErrorMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Stream::getAlready)
                    .build()
            );

            return;
        }

        if (!isStream && !needStart) {
            sendErrorMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Stream::getNot)
                    .build()
            );

            return;
        }

        fPlayer.setSetting(SettingText.STREAM_PREFIX, needStart ? localization().getPrefixTrue() : null);
        fPlayerService.saveOrUpdateSetting(fPlayer, SettingText.STREAM_PREFIX);

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
                    .range(config().getRange())
                    .destination(config().getDestination())
                    .sound(getModuleSound())
                    .proxy(dataOutputStream -> dataOutputStream.writeString(urls))
                    .integration(string -> Strings.CS.replace(string, "<urls>", StringUtils.defaultString(urls)))
                    .build()
            );
        } else {
            sendMessage(StreamMetadata.<Localization.Command.Stream>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Stream::getFormatEnd)
                    .turned(false)
                    .destination(config().getDestination())
                    .build()
            );
        }
    }

    @Override
    public MessageType messageType() {
        return MessageType.COMMAND_STREAM;
    }

    @Override
    public Command.Stream config() {
        return fileResolver.getCommand().getStream();
    }

    @Override
    public Permission.Command.Stream permission() {
        return fileResolver.getPermission().getCommand().getStream();
    }

    @Override
    public Localization.Command.Stream localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getCommand().getStream();
    }

    public void addTag(MessageContext messageContext) {
        if (messageContext.isFlag(MessageFlag.USER_MESSAGE)) return;

        FEntity sender = messageContext.getSender();
        if (!(sender instanceof FPlayer fPlayer)) return;
        if (isModuleDisabledFor(fPlayer)) return;

        messageContext.addReplacementTag(MessagePipeline.ReplacementTag.STREAM_PREFIX, (argumentQueue, context) -> {
            String streamPrefix = fPlayer.getSetting(SettingText.STREAM_PREFIX);
            if (StringUtils.isEmpty(streamPrefix)) return Tag.selfClosingInserting(Component.empty());

            return Tag.preProcessParsed(streamPrefix);
        });
    }

    public Function<Localization.Command.Stream, String> replaceUrls(String string) {
        return message -> {
            List<String> urls = Arrays.stream(string.split(" "))
                    .map(url -> Strings.CS.replace(message.getUrlTemplate(), "<url>", url))
                    .toList();

            return Strings.CS.replace(message.getFormatStart(), "<urls>", String.join("<br>", urls));
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
