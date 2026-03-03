package net.flectone.pulse.module.command.ignore;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.module.command.ignore.model.Ignore;
import net.flectone.pulse.module.command.ignore.model.IgnoreMetadata;
import net.flectone.pulse.platform.controller.ModuleCommandController;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.incendo.cloud.context.CommandContext;

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class IgnoreModule implements ModuleCommand<Localization.Command.Ignore> {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
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
            messageDispatcher.dispatchError(this, EventMetadata.<Localization.Command.Ignore>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Ignore::myself)
                    .build()
            );

            return;
        }

        FPlayer fTarget = fPlayerService.getFPlayer(targetName);
        if (fTarget.isUnknown()) {
            messageDispatcher.dispatchError(this, EventMetadata.<Localization.Command.Ignore>builder()
                    .sender(fPlayer)
                    .format(Localization.Command.Ignore::nullPlayer)
                    .build()
            );

            return;
        }

        Optional<Ignore> optionalIgnore = fPlayer.ignores()
                .stream()
                .filter(i -> i.target() == fTarget.id())
                .findFirst();

        Ignore metadataIgnore = optionalIgnore.orElse(null);
        if (optionalIgnore.isPresent()) {
            fPlayer = fPlayerService.deleteIgnore(fPlayer, optionalIgnore.get());
        } else {
            fPlayer = fPlayerService.saveIgnore(fPlayer, fTarget);

            if (fPlayer.ignores().isEmpty()) return;
            metadataIgnore = fPlayer.ignores().getLast();
        }

        messageDispatcher.dispatch(this, IgnoreMetadata.<Localization.Command.Ignore>builder()
                .base(EventMetadata.<Localization.Command.Ignore>builder()
                        .sender(fPlayer)
                        .format(ignore -> optionalIgnore.isEmpty() ? ignore.formatTrue() : ignore.formatFalse())
                        .destination(config().destination())
                        .tagResolvers(fResolver -> new TagResolver[]{
                                messagePipeline.targetTag(fResolver, fTarget)
                        })
                        .sound(soundOrThrow())
                        .build()
                )
                .ignore(metadataIgnore)
                .ignored(optionalIgnore.isEmpty())
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
    public Localization.Command.Ignore localization(FEntity sender) {
        return fileFacade.localization(sender).command().ignore();
    }
}
