package net.flectone.pulse.module.command.nickname;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.nickname.listener.NicknamePulseListener;
import net.flectone.pulse.module.command.nickname.model.NicknameMetadata;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.apache.commons.lang3.Strings;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.meta.CommandMeta;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class NicknameModule extends AbstractModuleCommand<Localization.Command.Nickname> {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final CommandParserProvider commandParserProvider;
    private final PermissionChecker permissionChecker;
    private final ListenerRegistry listenerRegistry;
    private final MessagePipeline messagePipeline;
    private final ProxyRegistry proxyRegistry;
    private final ProxySender proxySender;
    private final FLogger fLogger;

    private Pattern allowedPattern;

    @Override
    public void onEnable() {
        super.onEnable();

        if (!config().allowedInput().isEmpty()) {
            try {
                allowedPattern = Pattern.compile(config().allowedInput());
            } catch (PatternSyntaxException e) {
                fLogger.warning(e);
                return;
            }
        }

        String promptMessage = addPrompt(0, Localization.Command.Prompt::message);
        registerCommand(commandBuilder -> commandBuilder
                .permission(permission().name())
                .required(promptMessage, commandParserProvider.nativeMessageParser())
        );

        String promptPlayer = addPrompt(1, Localization.Command.Prompt::player);
        registerCustomCommand(manager ->
                manager.commandBuilder(getCommandName() + "other", CommandMeta.empty())
                        .permission(permission().other().name())
                        .required(promptPlayer, commandParserProvider.playerParser())
                        .required(promptMessage, commandParserProvider.nativeMessageParser())
                        .handler(commandContext -> executeOther(commandContext.sender(), commandContext))
        );

        listenerRegistry.register(NicknamePulseListener.class);
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer, true)) return;

        String nick = getArgument(commandContext, 0);

        changeName(fPlayer, fPlayer, nick);
    }

    public void executeOther(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer, true)) return;

        String playerName = getArgument(commandContext, 1);
        FPlayer fTarget = fPlayerService.getFPlayer(playerName);
        if (fTarget.isUnknown() || !fTarget.isOnline()) {
            sendErrorMessage(EventMetadata.<Localization.Command.Nickname>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Nickname::nullPlayer)
                    .build()
            );

            return;
        }

        fTarget = fPlayerService.loadSettings(fTarget);

        String nick = getArgument(commandContext, 0);

        changeName(fPlayer, fTarget, nick);
    }

    @Override
    public MessageType messageType() {
        return MessageType.COMMAND_NICKNAME;
    }

    @Override
    public Command.Nickname config() {
        return fileFacade.command().nickname();
    }

    @Override
    public Permission.Command.Nickname permission() {
        return fileFacade.permission().command().nickname();
    }

    @Override
    public Localization.Command.Nickname localization(FEntity sender) {
        return fileFacade.localization(sender).command().nickname();
    }

    @Override
    public ImmutableList.Builder<PermissionSetting> permissionBuilder() {
        return super.permissionBuilder()
                .add(permission().see(), permission().other());
    }

    public void changeName(FPlayer fPlayer, FPlayer fTarget, String nickname) {
        if (allowedPattern != null && !allowedPattern.matcher(nickname).matches()) {
            sendErrorMessage(EventMetadata.<Localization.Command.Nickname>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Nickname::nullNickname)
                    .build()
            );

            return;
        }

        boolean needClear = "clear".equalsIgnoreCase(nickname) || fTarget.name().equalsIgnoreCase(nickname);
        fTarget = needClear
                ? fTarget.withoutSetting(SettingText.NICKNAME)
                : fTarget.withSetting(SettingText.NICKNAME, nickname);

        fPlayerService.saveOrUpdateSetting(fTarget, SettingText.NICKNAME);

        sendMessage(NicknameMetadata.<Localization.Command.Nickname>builder()
                .base(EventMetadata.<Localization.Command.Nickname>builder()
                        .sender(fTarget)
                        .format(Localization.Command.Nickname::format)
                        .destination(config().destination())
                        .sound(soundOrThrow())
                        .build()
                )
                .nickname(nickname)
                .build()
        );

        if (proxyRegistry.hasEnabledProxy()) {
            proxySender.send(fTarget, MessageType.COMMAND_NICKNAME);
        }
    }

    public MessageContext addTag(MessageContext messageContext) {
        return messageContext.addTagResolver(MessagePipeline.ReplacementTag.NICKNAME, (argumentQueue, context) -> {
            String value = fPlayerService.getFPlayer(messageContext.sender()).getSetting(SettingText.NICKNAME);
            if (value == null) return Tag.preProcessParsed(messageContext.sender().name());

            Localization.Command.Nickname localization = localization(messageContext.receiver());
            String displayFormat = Strings.CS.replace(
                    permissionChecker.check(messageContext.receiver(), permission().see()) ? localization.displaySee() : localization.display(),
                    "<value>",
                    value
            );

            MessageContext nickContext = messagePipeline.createContext(messageContext.sender(), messageContext.receiver(), displayFormat)
                    .withFlags(messageContext.flags())
                    .addFlags(
                            new MessageFlag[]{MessageFlag.USER_MESSAGE, MessageFlag.NICKNAME},
                            new boolean[]{false, false}
                    );

            Component nickComponent = messagePipeline.build(nickContext);

            return Tag.inserting(nickComponent);
        });
    }

}
