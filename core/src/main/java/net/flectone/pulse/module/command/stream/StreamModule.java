package net.flectone.pulse.module.command.stream;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.NonNull;
import net.flectone.pulse.configuration.Command;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.constant.DisableSource;
import net.flectone.pulse.constant.MessageType;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.stream.listener.StreamPulseListener;
import net.flectone.pulse.provider.CommandParserProvider;
import net.flectone.pulse.registry.ListenerRegistry;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
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
        super(localization -> localization.getCommand().getStream(), Command::getStream);

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
        if (checkModulePredicates(fPlayer)) return;
        if (checkCooldown(fPlayer)) return;
        if (checkDisable(fPlayer, fPlayer, DisableSource.YOU)) return;
        if (checkMute(fPlayer)) return;

        String type = getArgument(commandContext, 0);
        Boolean needStart = switch (type) {
            case "start" -> true;
            case "end" -> false;
            default -> null;
        };

        if (needStart == null) return;

        boolean isStream = fPlayer.isSetting(FPlayer.Setting.STREAM);

        if (isStream && needStart && !fPlayer.isUnknown()) {
            builder(fPlayer)
                    .format(Localization.Command.Stream::getAlready)
                    .sendBuilt();
            return;
        }

        if (!isStream && !needStart) {
            builder(fPlayer)
                    .format(Localization.Command.Stream::getNot)
                    .sendBuilt();
            return;
        }

        setStreamPrefix(fPlayer, needStart);

        if (needStart) {
            String promptUrl = getPrompt(1);
            Optional<String> optionalUrl = commandContext.optional(promptUrl);
            String rawString = optionalUrl.orElse("");

            String urls = Arrays.stream(rawString.split("\\s+"))
                    .filter(this::isUrl)
                    .collect(Collectors.joining(" "));

            builder(fPlayer)
                    .range(command.getRange())
                    .destination(command.getDestination())
                    .tag(MessageType.COMMAND_STREAM)
                    .format(replaceUrls(urls))
                    .proxy(output -> output.writeUTF(urls))
                    .integration(s -> s.replace("<urls>", urls))
                    .sound(getSound())
                    .sendBuilt();

        } else {
            builder(fPlayer)
                    .destination(command.getDestination())
                    .format(Localization.Command.Stream::getFormatEnd)
                    .sendBuilt();
        }
    }

    public Function<Localization.Command.Stream, String> replaceUrls(String string) {
        return message -> {
            List<String> urls = Arrays.stream(string.split(" "))
                    .map(url -> message.getUrlTemplate().replace("<url>", url))
                    .toList();

            return message.getFormatStart()
                    .replace("<urls>", String.join("<br>", urls));
        };
    }

    public void setStreamPrefix(FPlayer fPlayer, boolean isStart) {
        if (checkModulePredicates(fPlayer)) return;
        if (fPlayer.isUnknown()) return;

        if (isStart) {
            fPlayerService.saveOrUpdateSetting(fPlayer, FPlayer.Setting.STREAM, "");
            fPlayerService.saveOrUpdateSetting(fPlayer, FPlayer.Setting.STREAM_PREFIX, resolveLocalization().getPrefixTrue());
            return;
        }

        fPlayerService.deleteSetting(fPlayer, FPlayer.Setting.STREAM);
        fPlayerService.saveOrUpdateSetting(fPlayer, FPlayer.Setting.STREAM_PREFIX, resolveLocalization().getPrefixFalse());
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
