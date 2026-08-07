package net.flectone.pulse.module.command.ignore;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.constant.SettingText;
import net.flectone.pulse.dispatcher.MessageDispatcher;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.command.ignore.model.Ignore;
import net.flectone.pulse.module.command.ignore.model.IgnoreMessageContext;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.platform.controller.ModuleCommandController;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.SocialService;
import org.incendo.cloud.context.CommandContext;

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class IgnoreModuleImpl implements IgnoreModule {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final SocialService socialService;
    private final CommandParserProvider commandParserProvider;
    private final MessagePipeline messagePipeline;
    private final MessageDispatcher messageDispatcher;
    private final ModuleController moduleController;
    private final ModuleCommandController commandModuleController;

    @Override
    public void onEnable() {
        String promptPlayer = commandModuleController.addPrompt(this, 0, Localization.Command.Prompt::player);
        commandModuleController.registerCommand(this, manager -> manager
                .permission(permission().name())
                .required(promptPlayer, commandParserProvider.playerParser(config().suggestOfflinePlayers()))
        );
    }

    @Override
    public void onDisable() {
        commandModuleController.clearPrompts(this);
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (moduleController.isDisabledFor(this, fPlayer, true)) return;

        String targetName = commandModuleController.getArgument(this, commandContext, 0);

        if (fPlayer.name().equalsIgnoreCase(targetName)) {
            messageDispatcher.dispatch(ModuleName.ERROR, EventMetadata.builder()
                    .messageContext(fResolver -> MessageContext.builder()
                            .sender(fPlayer)
                            .receiver(fResolver)
                            .message(localization(fResolver).myself())
                            .build()
                    )
                    .build()
            );

            return;
        }

        FPlayer fTarget = fPlayerService.getFPlayer(targetName);
        if (fTarget.isUnknown()) {
            messageDispatcher.dispatch(ModuleName.ERROR, EventMetadata.builder()
                    .messageContext(fResolver -> MessageContext.builder()
                            .sender(fPlayer)
                            .receiver(fResolver)
                            .message(localization(fPlayer).nullPlayer())
                            .build()
                    )
                    .build()
            );

            return;
        }


        Optional<Ignore> optionalIgnore = socialService.loadIgnores(fPlayer)
                .stream()
                .filter(i -> i.target() == fTarget.id())
                .findAny();

        Ignore metadataIgnore;
        if (optionalIgnore.isPresent()) {
            Ignore ignore = optionalIgnore.get();

            socialService.deleteIgnore(fPlayer, ignore);

            metadataIgnore = ignore;
        } else {
            Optional<Ignore> ignore = socialService.saveIgnore(fPlayer, fTarget);
            if (ignore.isEmpty()) return;

            metadataIgnore = ignore.get();
        }

        messageDispatcher.dispatch(this, EventMetadata.builder()
                .destination(config().destination())
                .sound(soundOrThrow())
                .messageContext(fResolver -> IgnoreMessageContext.builder()
                        .base(MessageContext.builder()
                                .sender(fPlayer)
                                .receiver(fResolver)
                                .message(optionalIgnore.isEmpty() ? localization(fResolver).formatTrue() : localization(fResolver).formatFalse())
                                .tagResolver(messagePipeline.targetTag(fResolver, fTarget))
                                .build()
                        )
                        .ignore(metadataIgnore)
                        .ignored(optionalIgnore.isEmpty())
                        .build()
                )
                .build()
        );
    }

    @Override
    public ModuleName name() {
        return ModuleName.COMMAND_IGNORE;
    }

    @Override
    public Command.Ignore config() {
        return fileFacade.command().ignore();
    }

    @Override
    public Permission.Command.Ignore permission() {
        return fileFacade.permission().command().ignore();
    }

    @Override
    public Localization.Command.Ignore localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).command().ignore();
    }
}
