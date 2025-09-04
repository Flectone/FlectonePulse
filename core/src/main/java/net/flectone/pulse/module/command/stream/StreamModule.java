package net.flectone.pulse.module.command.stream;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.NonNull;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
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

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Singleton
public class StreamModule extends AbstractModuleCommand<Localization.Command.Stream> implements PulseListener {

    private final Command.Stream command;
    private final Permission.Command.Stream permission;
    private final FPlayerService fPlayerService;
    private final CommandParserProvider commandParserProvider;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public StreamModule(FileResolver fileResolver,
                        FPlayerService fPlayerService,
                        CommandParserProvider commandParserProvider,
                        ListenerRegistry listenerRegistry) {
        super(localization -> localization.getCommand().getStream(), Command::getStream, MessageType.COMMAND_STREAM);

        this.command = fileResolver.getCommand().getStream();
        this.permission = fileResolver.getPermission().getCommand().getStream();
        this.fPlayerService = fPlayerService;
        this.commandParserProvider = commandParserProvider;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        String promptType = addPrompt(0, Localization.Command.Prompt::getType);
        String promptUrl = addPrompt(1, Localization.Command.Prompt::getUrl);
        registerCommand(manager -> manager
                .permission(permission.getName())
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
            sendMessage(MessageType.ERROR, metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Stream::getAlready)
                    .build()
            );

            return;
        }

        if (!isStream && !needStart) {
            sendMessage(MessageType.ERROR, metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Stream::getNot)
                    .build()
            );

            return;
        }

        fPlayer.setSetting(SettingText.STREAM_PREFIX, needStart ? resolveLocalization().getPrefixTrue() : null);
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
                    .range(command.getRange())
                    .destination(command.getDestination())
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
                    .destination(command.getDestination())
                    .build()
            );
        }
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
            new URL(string).toURI();
            return true;
        } catch (MalformedURLException | URISyntaxException e) {
            return false;
        }
    }
}
