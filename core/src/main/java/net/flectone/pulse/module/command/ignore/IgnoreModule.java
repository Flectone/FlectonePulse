package net.flectone.pulse.module.command.ignore;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Command;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.model.Ignore;
import net.flectone.pulse.registry.CommandRegistry;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.constant.DisableSource;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.meta.CommandMeta;

import java.util.Optional;

@Singleton
public class IgnoreModule extends AbstractModuleCommand<Localization.Command.Ignore> {

    private final Command.Ignore command;
    private final Permission.Command.Ignore permission;
    private final FPlayerService fPlayerService;
    private final CommandRegistry commandRegistry;

    @Inject
    public IgnoreModule(FileResolver fileResolver,
                        FPlayerService fPlayerService,
                        CommandRegistry commandRegistry) {
        super(localization -> localization.getCommand().getIgnore(), null);

        this.command = fileResolver.getCommand().getIgnore();
        this.permission = fileResolver.getPermission().getCommand().getIgnore();
        this.fPlayerService = fPlayerService;
        this.commandRegistry = commandRegistry;
    }

    @Override
    protected boolean isConfigEnable() {
        return command.isEnable();
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        String commandName = getName(command);
        String promptPlayer = getPrompt().getPlayer();
        commandRegistry.registerCommand(manager ->
                manager.commandBuilder(commandName, command.getAliases(), CommandMeta.empty())
                        .permission(permission.getName())
                        .required(promptPlayer, commandRegistry.playerParser(command.isSuggestOfflinePlayers()))
                        .handler(this)
        );

        addPredicate(this::checkCooldown);
        addPredicate(fPlayer -> checkDisable(fPlayer, fPlayer, DisableSource.YOU));
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (checkCooldown(fPlayer)) return;
        if (checkDisable(fPlayer, fPlayer, DisableSource.YOU)) return;
        if (checkModulePredicates(fPlayer)) return;

        String prompt = getPrompt().getPlayer();
        String targetName = commandContext.get(prompt);

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
