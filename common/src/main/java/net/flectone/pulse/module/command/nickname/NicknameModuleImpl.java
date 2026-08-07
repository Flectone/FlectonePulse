package net.flectone.pulse.module.command.nickname;

import java.util.LinkedHashSet;
import java.util.Collections;
import java.util.Set;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.command.nickname.listener.NicknameProxyMessageListener;
import net.flectone.pulse.module.command.nickname.listener.PulseNicknameListener;
import net.flectone.pulse.module.command.nickname.model.NicknameMessageContext;
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

import java.util.UUID;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class NicknameModuleImpl implements NicknameModule {

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

    @Override
    public void executeOther(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (moduleController.isDisabledFor(this, fPlayer, true)) return;

        String playerName = commandModuleController.getArgument(this, commandContext, 1);
        FPlayer fTarget = fPlayerService.getFPlayer(playerName);
        if (fTarget.isUnknown() || !fTarget.isOnline()) {
            messageDispatcher.dispatch(ModuleName.ERROR, EventMetadata.builder()
                    .messageContext(fResolver -> MessageContext.builder()
                            .sender(fPlayer)
                            .receiver(fResolver)
                            .message(localization(fResolver).nullPlayer())
                            .build()
                    )
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
    public Set<PermissionSetting> permissions() {
        Set<PermissionSetting> permissions = new LinkedHashSet<>(NicknameModule.super.permissions());
        permissions.add(permission().see());
        permissions.add(permission().other());
        return Collections.unmodifiableSet(permissions);
    }

    @Override
    public void changeName(FPlayer fPlayer, FPlayer fTarget, String nickname) {
        boolean needClear = "clear".equalsIgnoreCase(nickname) || fTarget.name().equalsIgnoreCase(nickname);

        if (!needClear && allowedPredicate != null && !allowedPredicate.test(nickname)) {
            messageDispatcher.dispatch(ModuleName.ERROR, EventMetadata.builder()
                    .messageContext(fResolver -> MessageContext.builder()
                            .sender(fPlayer)
                            .receiver(fResolver)
                            .message(localization(fResolver).nullNickname())
                            .build()
                    )
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

        if (proxySender.send(fTarget, ModuleName.COMMAND_NICKNAME, dataOutputStream -> dataOutputStream.writeUTF(nickname))) return;

        sendMessageWithUpdatedNickname(fPlayer, nickname, UUID.randomUUID());
    }

    @Override
    public void sendMessageWithUpdatedNickname(FEntity fPlayer, String nickname, UUID metadataUUID) {
        messageDispatcher.dispatch(this, EventMetadata.builder()
                .destination(config().destination())
                .sound(soundOrThrow())
                .messageContext(fResolver -> NicknameMessageContext.builder()
                        .base(MessageContext.builder()
                                .uuid(metadataUUID)
                                .sender(fPlayer)
                                .receiver(fResolver)
                                .message(localization(fResolver).format())
                                .build()
                        )
                        .nickname(nickname)
                        .build()
                )
                .build()
        );
    }

    @Override
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
