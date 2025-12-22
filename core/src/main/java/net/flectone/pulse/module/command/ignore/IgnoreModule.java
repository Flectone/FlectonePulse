package net.flectone.pulse.module.command.ignore;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.ignore.model.Ignore;
import net.flectone.pulse.module.command.ignore.model.IgnoreMetadata;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageType;
import org.incendo.cloud.context.CommandContext;

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class IgnoreModule extends AbstractModuleCommand<Localization.Command.Ignore> {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final CommandParserProvider commandParserProvider;

    @Override
    public void onEnable() {
        super.onEnable();

        String promptPlayer = addPrompt(0, Localization.Command.Prompt::player);
        registerCommand(manager -> manager
                .permission(permission().name())
                .required(promptPlayer, commandParserProvider.playerParser(config().suggestOfflinePlayers()))
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer, true)) return;

        String targetName = getArgument(commandContext, 0);

        if (fPlayer.getName().equalsIgnoreCase(targetName)) {
            sendErrorMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Ignore::myself)
                    .build()
            );

            return;
        }

        FPlayer fTarget = fPlayerService.getFPlayer(targetName);
        if (fTarget.isUnknown()) {
            sendErrorMessage(metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Ignore::nullPlayer)
                    .build()
            );

            return;
        }

        Optional<Ignore> optionalIgnore = fPlayer.getIgnores()
                .stream()
                .filter(i -> i.target() == fTarget.getId())
                .findFirst();

        Ignore metadataIgnore;

        if (optionalIgnore.isPresent()) {
            metadataIgnore = optionalIgnore.get();
            fPlayer.getIgnores().remove(optionalIgnore.get());
            fPlayerService.deleteIgnore(optionalIgnore.get());
        } else {
            Ignore newIgnore = fPlayerService.saveAndGetIgnore(fPlayer, fTarget);
            if (newIgnore == null) return;

            metadataIgnore = newIgnore;
            fPlayer.getIgnores().add(newIgnore);
        }

        sendMessage(IgnoreMetadata.<Localization.Command.Ignore>builder()
                .sender(fTarget)
                .filterPlayer(fPlayer)
                .format(ignore -> optionalIgnore.isEmpty() ? ignore.formatTrue() : ignore.formatFalse())
                .ignore(metadataIgnore)
                .ignored(optionalIgnore.isEmpty())
                .destination(config().destination())
                .sound(soundOrThrow())
                .build()
        );
    }

    @Override
    public MessageType messageType() {
        return MessageType.COMMAND_IGNORE;
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
