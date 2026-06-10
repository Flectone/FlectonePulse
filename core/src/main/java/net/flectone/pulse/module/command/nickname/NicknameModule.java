package net.flectone.pulse.module.command.nickname;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.module.command.nickname.listener.NicknameProxyMessageListener;
import net.flectone.pulse.module.command.nickname.listener.PulseNicknameListener;
import net.flectone.pulse.module.command.nickname.model.NicknameMetadata;
import net.flectone.pulse.platform.controller.ModuleCommandController;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.processing.resolver.ProfileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.apache.commons.lang3.Strings;
import org.incendo.cloud.context.CommandContext;

import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class NicknameModule implements ModuleCommand<Localization.Command.Nickname> {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final SocialService socialService;
    private final CommandParserProvider commandParserProvider;
    private final PermissionChecker permissionChecker;
    private final ListenerRegistry listenerRegistry;
    private final MessagePipeline messagePipeline;
    private final MessageDispatcher messageDispatcher;
    private final ProxyRegistry proxyRegistry;
    private final ProxySender proxySender;
    private final ModuleController moduleController;
    private final ModuleCommandController commandModuleController;
    private final ProfileResolver profileResolver;
    private final FLogger fLogger;

    private Predicate<String> allowedPredicate;

    @Override
    public void onEnable() {
        if (!config().allowedInput().isEmpty()) {
            try {
                allowedPredicate = Pattern.compile(config().allowedInput()).asMatchPredicate();
            } catch (PatternSyntaxException e) {
                fLogger.warning(e);
                return;
            }
        }

        String promptMessage = commandModuleController.addPrompt(this, 0, Localization.Command.Prompt::message);
        commandModuleController.registerCommand(this, commandBuilder -> commandBuilder
                .permission(permission().name())
                .required(promptMessage, commandParserProvider.nativeMessageParser())
        );

        String promptPlayer = commandModuleController.addPrompt(this, 1, Localization.Command.Prompt::player);
        commandModuleController.registerSubCommand(this, config().subCommandOther(), commandBuilder -> commandBuilder
                .permission(permission().other().name())
                .required(promptPlayer, commandParserProvider.playerParser())
                .required(promptMessage, commandParserProvider.nativeMessageParser())
                .handler(commandContext -> executeOther(commandContext.sender(), commandContext))
        );

        if (proxyRegistry.hasEnabledProxy()) {
            listenerRegistry.register(NicknameProxyMessageListener.class);
        }

        listenerRegistry.register(PulseNicknameListener.class);
    }

    @Override
    public void onDisable() {
        commandModuleController.clearPrompts(this);
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (moduleController.isDisabledFor(this, fPlayer, true)) return;

        String nick = commandModuleController.getArgument(this, commandContext, 0);

        changeName(fPlayer, fPlayer, nick);
    }

    public void executeOther(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (moduleController.isDisabledFor(this, fPlayer, true)) return;

        String playerName = commandModuleController.getArgument(this, commandContext, 1);
        FPlayer fTarget = fPlayerService.getFPlayer(playerName);
        if (fTarget.isUnknown() || !fTarget.isOnline()) {
            messageDispatcher.dispatchError(this, EventMetadata.<Localization.Command.Nickname>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Nickname::nullPlayer)
                    .build()
            );

            return;
        }

        String nick = commandModuleController.getArgument(this, commandContext, 0);

        changeName(fPlayer, fTarget, nick);
    }

    @Override
    public ModuleName name() {
        return ModuleName.COMMAND_NICKNAME;
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
    public Localization.Command.Nickname localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).command().nickname();
    }

    @Override
    public ImmutableSet.Builder<PermissionSetting> permissionBuilder() {
        return ModuleCommand.super.permissionBuilder().add(permission().see(), permission().other());
    }

    public void changeName(FPlayer fPlayer, FPlayer fTarget, String nickname) {
        boolean needClear = "clear".equalsIgnoreCase(nickname) || fTarget.name().equalsIgnoreCase(nickname);

        if (!needClear && allowedPredicate != null && !allowedPredicate.test(nickname)) {
            messageDispatcher.dispatchError(this, EventMetadata.<Localization.Command.Nickname>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Nickname::nullNickname)
                    .build()
            );

            return;
        }

        if (needClear) {
            if (socialService.getSetting(fTarget, SettingText.NICKNAME) != null) {
                socialService.saveSetting(fTarget, SettingText.NICKNAME, null);
            }
        } else {
            socialService.saveSetting(fTarget, SettingText.NICKNAME, nickname);
        }

        messageDispatcher.dispatch(this, NicknameMetadata.<Localization.Command.Nickname>builder()
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
            proxySender.send(fTarget, ModuleName.COMMAND_NICKNAME);
        }
    }

    public MessageContext addTag(MessageContext messageContext) {
        return messageContext.addTagResolver(messagePipeline.resolver(MessagePipeline.ReplacementTag.NICKNAME.getTagName(), (_, _) -> {
            // get nickname value
            String value = socialService.getSetting(fPlayerService.getFPlayer(messageContext.sender()), SettingText.NICKNAME);

            // resolve receiver localization
            Localization.Command.Nickname localization = localization(messageContext.receiver());

            if (value == null) {
                String defaultNickname = localization.defaultNickname();

                // skip module formatting
                if (Strings.CS.equals(defaultNickname, "<player>")) {
                    return Tag.preProcessParsed(profileResolver.resolveName(messageContext.sender()));
                }

                value = defaultNickname;
            }

            return Tag.inserting(messagePipeline.build(MessageContext.builder()
                    .sender(messageContext.sender())
                    .receiver(messageContext.receiver())
                    .message(Strings.CS.replace(
                            permissionChecker.check(messageContext.receiver(), permission().see()) ? localization.displaySee() : localization.display(),
                            "<value>",
                            value
                    ))
                    .flags(messageContext.flags())
                    .flags(
                            new MessageFlag[]{MessageFlag.PLAYER_MESSAGE, MessageFlag.NICKNAME_MODULE, MessageFlag.ICU_MODULE},
                            new boolean[]{false, false, true}
                    )
                    .build()
            ));
        }));
    }

}
