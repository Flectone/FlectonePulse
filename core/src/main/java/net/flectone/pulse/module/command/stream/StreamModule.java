package net.flectone.pulse.module.command.stream;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.NonNull;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.configuration.Command;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.context.MessageContext;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.processor.MessageProcessor;
import net.flectone.pulse.registry.CommandRegistry;
import net.flectone.pulse.registry.EventProcessRegistry;
import net.flectone.pulse.registry.MessageProcessRegistry;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.DisableAction;
import net.flectone.pulse.constant.MessageType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.meta.CommandMeta;
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
public class StreamModule extends AbstractModuleCommand<Localization.Command.Stream> implements MessageProcessor {

    @Getter private final Command.Stream command;
    private final Permission.Command.Stream permission;
    private final Permission.Message.Format formatPermission;
    private final FPlayerService fPlayerService;
    private final CommandRegistry commandRegistry;
    private final PermissionChecker permissionChecker;
    private final MessageProcessRegistry messageProcessRegistry;
    private final EventProcessRegistry eventProcessRegistry;

    @Inject
    public StreamModule(FileResolver fileResolver,
                        FPlayerService fPlayerService,
                        CommandRegistry commandRegistry,
                        PermissionChecker permissionChecker,
                        MessageProcessRegistry messageProcessRegistry,
                        EventProcessRegistry eventProcessRegistry) {
        super(localization -> localization.getCommand().getStream(), null);

        this.command = fileResolver.getCommand().getStream();
        this.permission = fileResolver.getPermission().getCommand().getStream();
        this.formatPermission = fileResolver.getPermission().getMessage().getFormat();
        this.fPlayerService = fPlayerService;
        this.commandRegistry = commandRegistry;
        this.permissionChecker = permissionChecker;
        this.messageProcessRegistry = messageProcessRegistry;
        this.eventProcessRegistry = eventProcessRegistry;
    }

    @Override
    protected boolean isConfigEnable() {
        return command.isEnable();
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        String commandName = getName(command);
        String promptType = getPrompt().getType();
        String promptUrl = getPrompt().getUrl();
        commandRegistry.registerCommand(manager ->
                manager.commandBuilder(commandName, command.getAliases(), CommandMeta.empty())
                        .permission(permission.getName())
                        .required(promptType, commandRegistry.singleMessageParser(), typeSuggestion())
                        .optional(promptUrl, commandRegistry.nativeMessageParser())
                        .handler(this)
        );

        messageProcessRegistry.register(150, this);
        eventProcessRegistry.registerPlayerHandler(Event.Type.PLAYER_LOAD, fPlayer ->
                setStreamPrefix(fPlayer, fPlayer.isSetting(FPlayer.Setting.STREAM))
        );
    }

    @Override
    public void process(MessageContext messageContext) {
        FEntity sender = messageContext.getSender();
        if (messageContext.isUserMessage() && !permissionChecker.check(sender, formatPermission.getAll())) return;
        if (!(sender instanceof FPlayer fPlayer)) return;
        if (checkModulePredicates(fPlayer)) return;

        messageContext.addReplacementTag(MessagePipeline.ReplacementTag.STREAM_PREFIX, (argumentQueue, context) -> {
            String streamPrefix = fPlayer.getSettingValue(FPlayer.Setting.STREAM_PREFIX);
            if (streamPrefix == null) return Tag.selfClosingInserting(Component.empty());

            return Tag.preProcessParsed(streamPrefix);
        });
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
        if (checkDisable(fPlayer, fPlayer, DisableAction.YOU)) return;
        if (checkMute(fPlayer)) return;

        String promptType = getPrompt().getType();
        String type = commandContext.get(promptType);
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
            String promptUrl = getPrompt().getUrl();
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
