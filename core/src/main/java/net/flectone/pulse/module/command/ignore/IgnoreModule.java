package net.flectone.pulse.module.command.ignore;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.ignore.model.Ignore;
import net.flectone.pulse.module.command.ignore.model.IgnoreMetadata;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageType;
import org.incendo.cloud.context.CommandContext;

import java.util.Optional;

@Singleton
public class IgnoreModule extends AbstractModuleCommand<Localization.Command.Ignore> {

    private final Command.Ignore command;
    private final Permission.Command.Ignore permission;
    private final FPlayerService fPlayerService;
    private final CommandParserProvider commandParserProvider;

    @Inject
    public IgnoreModule(FileResolver fileResolver,
                        FPlayerService fPlayerService,
                        CommandParserProvider commandParserProvider) {
        super(localization -> localization.getCommand().getIgnore(), Command::getIgnore, MessageType.COMMAND_IGNORE);

        this.command = fileResolver.getCommand().getIgnore();
        this.permission = fileResolver.getPermission().getCommand().getIgnore();
        this.fPlayerService = fPlayerService;
        this.commandParserProvider = commandParserProvider;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        String promptPlayer = addPrompt(0, Localization.Command.Prompt::getPlayer);
        registerCommand(manager -> manager
                .permission(permission.getName())
                .required(promptPlayer, commandParserProvider.playerParser(command.isSuggestOfflinePlayers()))
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer, true)) return;

        String targetName = getArgument(commandContext, 0);

        if (fPlayer.getName().equalsIgnoreCase(targetName)) {
            sendMessage(MessageType.ERROR, metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Ignore::getMyself)
                    .build()
            );

            return;
        }

        FPlayer fTarget = fPlayerService.getFPlayer(targetName);
        if (fTarget.isUnknown()) {
            sendMessage(MessageType.ERROR, metadataBuilder()
                    .sender(fPlayer)
                    .format(Localization.Command.Ignore::getNullPlayer)
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
                .format(ignore -> optionalIgnore.isEmpty() ? ignore.getFormatTrue() : ignore.getFormatFalse())
                .ignore(metadataIgnore)
                .ignored(optionalIgnore.isEmpty())
                .destination(command.getDestination())
                .sound(getModuleSound())
                .build()
        );
    }
}
