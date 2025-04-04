package net.flectone.pulse.module.command.clearchat;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.NonNull;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.configuration.Command;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.registry.CommandRegistry;
import net.flectone.pulse.service.FPlayerService;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.meta.CommandMeta;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;

import java.util.Collections;
import java.util.Optional;

@Singleton
public class ClearchatModule extends AbstractModuleCommand<Localization.Command.Clearchat> {

    private final Command.Clearchat command;
    private final Permission.Command.Clearchat permission;

    private final FPlayerService fPlayerService;
    private final CommandRegistry commandRegistry;
    private final PermissionChecker permissionChecker;

    @Inject
    public ClearchatModule(FPlayerService fPlayerService,
                           FileManager fileManager,
                           CommandRegistry commandRegistry,
                           PermissionChecker permissionChecker) {
        super(localization -> localization.getCommand().getClearchat(), null);

        this.fPlayerService = fPlayerService;
        this.commandRegistry = commandRegistry;
        this.permissionChecker = permissionChecker;

        command = fileManager.getCommand().getClearchat();
        permission = fileManager.getPermission().getCommand().getClearchat();

        addPredicate(this::checkCooldown);
    }

    @Override
    public boolean isConfigEnable() {
        return command.isEnable();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        registerPermission(permission.getOther());

        String commandName = getName(command);
        String promptPlayer = getPrompt().getPlayer();
        commandRegistry.registerCommand(manager ->
                manager.commandBuilder(commandName, command.getAliases(), CommandMeta.empty())
                        .permission(permission.getName())
                        .optional(promptPlayer, commandRegistry.playerParser(), playerSuggestionPermission())
                        .handler(this)
        );
    }

    private @NonNull BlockingSuggestionProvider<FPlayer> playerSuggestionPermission() {
        return (context, input) -> {
            if (!permissionChecker.check(context.sender(), permission.getOther())) return Collections.emptyList();

            return commandRegistry.playerParser().parser().suggestionProvider().suggestionsFuture(context, input).join();
        };
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (checkModulePredicates(fPlayer)) return;

        String promptPlayer = getPrompt().getPlayer();
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
