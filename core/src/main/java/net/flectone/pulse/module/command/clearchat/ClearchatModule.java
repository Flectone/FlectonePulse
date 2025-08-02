package net.flectone.pulse.module.command.clearchat;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.platform.provider.CommandParserProvider;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import org.incendo.cloud.context.CommandContext;

import java.util.Optional;

@Singleton
public class ClearchatModule extends AbstractModuleCommand<Localization.Command.Clearchat> {

    private final Command.Clearchat command;
    private final Permission.Command.Clearchat permission;
    private final FPlayerService fPlayerService;
    private final PermissionChecker permissionChecker;
    private final CommandParserProvider commandParserProvider;

    @Inject
    public ClearchatModule(FPlayerService fPlayerService,
                           FileResolver fileResolver,
                           PermissionChecker permissionChecker,
                           CommandParserProvider commandParserProvider) {
        super(localization -> localization.getCommand().getClearchat(), Command::getClearchat);

        this.command = fileResolver.getCommand().getClearchat();
        this.permission = fileResolver.getPermission().getCommand().getClearchat();
        this.fPlayerService = fPlayerService;
        this.permissionChecker = permissionChecker;
        this.commandParserProvider = commandParserProvider;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        registerPermission(permission.getOther());

        String promptPlayer = addPrompt(0, Localization.Command.Prompt::getPlayer);
        registerCommand(commandBuilder -> commandBuilder
                        .permission(permission.getName())
                        .optional(promptPlayer, commandParserProvider.playerParser(), commandParserProvider.playerSuggestionPermission(permission.getOther()))
        );

        addPredicate(this::checkCooldown);
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (checkModulePredicates(fPlayer)) return;

        String promptPlayer = getPrompt(0);
        Optional<String> optionalPlayer = commandContext.optional(promptPlayer);

        FPlayer fTarget = fPlayer;

        if (optionalPlayer.isPresent() && permissionChecker.check(fPlayer, permission.getOther())) {
            String player = optionalPlayer.get();
            if (player.equals("all") || player.equals("@a")) {
                fPlayerService.findOnlineFPlayers().forEach(this::clearChat);
                return;
            }

            fTarget = fPlayerService.getFPlayer(player);
            if (fTarget.isUnknown()) {
                builder(fPlayer)
                        .format(Localization.Command.Clearchat::getNullPlayer)
                        .sendBuilt();
                return;
            }
        }

        clearChat(fTarget);
    }

    private void clearChat(FPlayer fPlayer) {
        builder(fPlayer)
                .destination(command.getDestination())
                .format("<br> ".repeat(100))
                .sendBuilt();

        builder(fPlayer)
                .destination(command.getDestination())
                .format(Localization.Command.Clearchat::getFormat)
                .sound(getSound())
                .sendBuilt();
    }
}
