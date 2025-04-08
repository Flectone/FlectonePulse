package net.flectone.pulse.module.command.stream;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.NonNull;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.configuration.Command;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.context.MessageContext;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.processor.MessageProcessor;
import net.flectone.pulse.registry.CommandRegistry;
import net.flectone.pulse.registry.MessageProcessRegistry;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.DisableAction;
import net.flectone.pulse.util.MessageTag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.meta.CommandMeta;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.incendo.cloud.suggestion.Suggestion;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static net.flectone.pulse.util.TagResolverUtil.emptyTagResolver;

@Singleton
public class StreamModule extends AbstractModuleCommand<Localization.Command.Stream> implements MessageProcessor {

    @Getter private final Command.Stream command;
    private final Permission.Command.Stream permission;
    private final Permission.Message.Format formatPermission;

    private final FPlayerService fPlayerService;
    private final CommandRegistry commandRegistry;
    private final PermissionChecker permissionChecker;

    @Inject
    public StreamModule(FileManager fileManager,
                        FPlayerService fPlayerService,
                        CommandRegistry commandRegistry,
                        PermissionChecker permissionChecker,
                        MessageProcessRegistry messageProcessRegistry) {
        super(localization -> localization.getCommand().getStream(), null);

        this.fPlayerService = fPlayerService;
        this.commandRegistry = commandRegistry;
        this.permissionChecker = permissionChecker;

        command = fileManager.getCommand().getStream();
        permission = fileManager.getPermission().getCommand().getStream();
        formatPermission = fileManager.getPermission().getMessage().getFormat();

        messageProcessRegistry.register(150, this);
    }

    @Override
    public boolean isConfigEnable() {
        return command.isEnable();
    }

    @Override
    public void reload() {
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
    }

    @Override
    public void process(MessageContext messageContext) {
        FEntity sender = messageContext.getSender();
        if (messageContext.isUserMessage() && !permissionChecker.check(sender, formatPermission.getAll())) return;

        messageContext.addTagResolvers(streamTag(sender));
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

            builder(fPlayer)
                    .range(command.getRange())
                    .destination(command.getDestination())
                    .tag(MessageTag.COMMAND_STREAM)
                    .format(replaceUrls(rawString))
                    .proxy(output -> output.writeUTF(rawString))
                    .integration(s -> s.replace("<urls>", rawString))
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

    @Async
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

    private TagResolver streamTag(@NotNull FEntity sender) {
        String tag = "stream_prefix";
        if (!(sender instanceof FPlayer fPlayer)) return emptyTagResolver(tag);
        if (checkModulePredicates(fPlayer)) return emptyTagResolver(tag);

        return TagResolver.resolver(tag, (argumentQueue, context) -> {
            String streamPrefix = fPlayer.getSettingValue(FPlayer.Setting.STREAM_PREFIX);
            if (streamPrefix == null) return Tag.selfClosingInserting(Component.empty());

            return Tag.preProcessParsed(streamPrefix);
        });
    }
}
