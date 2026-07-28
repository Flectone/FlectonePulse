package net.flectone.pulse.module.command.ping;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.controller.ModuleCommandController;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import org.incendo.cloud.context.CommandContext;

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PingModule implements ModuleCommand {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final CommandParserProvider commandParserProvider;
    private final SocialService socialService;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final MessageDispatcher messageDispatcher;
    private final ModuleController moduleController;
    private final ModuleCommandController commandModuleController;

    @Override
    public void onEnable() {
        String promptPlayer = commandModuleController.addPrompt(this, 0, Localization.Command.Prompt::player);
        commandModuleController.registerCommand(this, commandBuilder -> commandBuilder
                .permission(permission().name())
                .optional(promptPlayer, commandParserProvider.platformPlayerParser())
        );
    }

    @Override
    public void onDisable() {
        commandModuleController.clearPrompts(this);
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (moduleController.isDisabledFor(this, fPlayer, true)) return;

        String promptPlayer = commandModuleController.getPrompt(this, 0);
        Optional<String> optionalTarget = commandContext.optional(promptPlayer);

        FPlayer fTarget = optionalTarget.isPresent() ? fPlayerService.getFPlayer(optionalTarget.get()) : fPlayer;
        if (!platformPlayerAdapter.isOnline(fTarget)
                || (!socialService.canSeeVanished(fTarget, fPlayer) && !fPlayer.equals(fTarget))) {
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

        messageDispatcher.dispatch(this, EventMetadata.builder()
                .filter(fPlayer)
                .destination(config().destination())
                .sound(soundOrThrow())
                .messageContext(fResolver -> MessageContext.builder()
                        .sender(fTarget)
                        .receiver(fResolver)
                        .message(localization(fResolver).format())
                        .build()
                )
                .build()
        );

    }

    @Override
    public ModuleName name() {
        return ModuleName.COMMAND_PING;
    }

    @Override
    public Command.Ping config() {
        return fileFacade.command().ping();
    }

    @Override
    public Permission.Command.Ping permission() {
        return fileFacade.permission().command().ping();
    }

    @Override
    public Localization.Command.Ping localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).command().ping();
    }
}
