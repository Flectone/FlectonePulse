package net.flectone.pulse.module.command.ignore;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.util.constant.DisableSource;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Ignore;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
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
        super(localization -> localization.getCommand().getIgnore(), Command::getIgnore);

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

        addPredicate(this::checkCooldown);
        addPredicate(fPlayer -> checkDisable(fPlayer, fPlayer, DisableSource.YOU));
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (checkCooldown(fPlayer)) return;
        if (checkDisable(fPlayer, fPlayer, DisableSource.YOU)) return;
        if (checkModulePredicates(fPlayer)) return;

        String targetName = getArgument(commandContext, 0);

        if (fPlayer.getName().equalsIgnoreCase(targetName)) {
            builder(fPlayer)
                    .format(Localization.Command.Ignore::getMyself)
                    .sendBuilt();
            return;
        }

        FPlayer fTarget = fPlayerService.getFPlayer(targetName);
        if (fTarget.isUnknown()) {
            builder(fPlayer)
                    .format(Localization.Command.Ignore::getNullPlayer)
                    .sendBuilt();
            return;
        }

        Optional<Ignore> ignore = fPlayer.getIgnores()
                .stream()
                .filter(i -> i.target() == fTarget.getId())
                .findFirst();

        if (ignore.isPresent()) {
            fPlayer.getIgnores().remove(ignore.get());
            fPlayerService.deleteIgnore(ignore.get());
        } else {
            Ignore newIgnore = fPlayerService.saveAndGetIgnore(fPlayer, fTarget);
            if (newIgnore == null) return;

            fPlayer.getIgnores().add(newIgnore);
        }

        builder(fTarget)
                .destination(command.getDestination())
                .receiver(fPlayer)
                .format(s -> ignore.isEmpty() ? s.getFormatTrue() : s.getFormatFalse())
                .sound(getSound())
                .sendBuilt();
    }
}
