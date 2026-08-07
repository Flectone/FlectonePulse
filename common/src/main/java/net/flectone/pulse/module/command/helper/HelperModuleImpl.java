package net.flectone.pulse.module.command.helper;

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
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.event.message.context.StringMessageContext;
import net.flectone.pulse.module.command.helper.listener.HelperProxyMessageListener;
import net.flectone.pulse.platform.controller.ModuleCommandController;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import org.incendo.cloud.context.CommandContext;

import java.util.List;
import java.util.function.Predicate;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class HelperModuleImpl implements HelperModule {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final ProxyRegistry proxyRegistry;
    private final ListenerRegistry listenerRegistry;
    private final PermissionChecker permissionChecker;
    private final CommandParserProvider commandParserProvider;
    private final MessageDispatcher messageDispatcher;
    private final MessagePipeline messagePipeline;
    private final ModuleController moduleController;
    private final ModuleCommandController commandModuleController;
    private final SocialService socialService;

    @Override
    public void onEnable() {
        String promptMessage = commandModuleController.addPrompt(this, 0, Localization.Command.Prompt::message);
        commandModuleController.registerCommand(this, commandBuilder -> commandBuilder
                .permission(permission().name())
                .required(promptMessage, commandParserProvider.nativeMessageParser())
        );

        if (proxyRegistry.hasEnabledProxy()) {
            listenerRegistry.register(HelperProxyMessageListener.class);
        }
    }

    @Override
    public void onDisable() {
        commandModuleController.clearPrompts(this);
    }

    @Override
    public Set<PermissionSetting> permissions() {
        Set<PermissionSetting> permissions = new LinkedHashSet<>(HelperModule.super.permissions());
        permissions.add(permission().see());
        return Collections.unmodifiableSet(permissions);
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (moduleController.isDisabledFor(this, fPlayer, true)) return;

        List<FPlayer> recipients = fPlayerService.getOnlineFPlayers()
                .stream()
                .filter(vanishedPlayer -> socialService.canSeeVanished(vanishedPlayer, fPlayer))
                .filter(getFilterSee())
                .toList();

        if (recipients.isEmpty() && config().nullHelper()) {
            messageDispatcher.dispatch(ModuleName.ERROR, EventMetadata.builder()
                    .messageContext(fResolver -> MessageContext.builder()
                            .sender(fPlayer)
                            .receiver(fResolver)
                            .message(localization(fResolver).nullHelper())
                            .build()
                    )
                    .build()
            );
            return;
        }

        String message = commandModuleController.getArgument(this, commandContext, 0);

        messageDispatcher.dispatch(this, EventMetadata.builder()
                .destination(config().destination())
                .messageContext(fResolver -> MessageContext.builder()
                        .sender(fPlayer)
                        .receiver(fResolver)
                        .message(localization(fResolver).player())
                        .build()
                )
                .build()
        );

        messageDispatcher.dispatch(this, EventMetadata.builder()
                .range(config().range())
                .filter(getFilterSee())
                .destination(config().destination())
                .sound(soundOrThrow())
                .messageContext(fResolver -> StringMessageContext.builder()
                        .base(MessageContext.builder()
                                .sender(fPlayer)
                                .receiver(fResolver)
                                .message(localization(fResolver).global())
                                .tagResolver(messagePipeline.messageTag(fPlayer, fResolver, message))
                                .build()
                        )
                        .string(message)
                        .build()
                )
                .proxy(dataOutputStream -> dataOutputStream.writeUTF(message))
                .integration()
                .build()
        );
    }

    @Override
    public ModuleName name() {
        return ModuleName.COMMAND_HELPER;
    }

    @Override
    public Command.Helper config() {
        return fileFacade.command().helper();
    }

    @Override
    public Permission.Command.Helper permission() {
        return fileFacade.permission().command().helper();
    }

    @Override
    public Localization.Command.Helper localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).command().helper();
    }

    @Override
    public Predicate<FPlayer> getFilterSee() {
        return fPlayer -> permissionChecker.check(fPlayer, permission().see());
    }
}
