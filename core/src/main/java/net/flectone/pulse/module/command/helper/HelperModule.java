package net.flectone.pulse.module.command.helper;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.configuration.Command;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.sender.ProxySender;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.registry.CommandRegistry;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.DisableAction;
import net.flectone.pulse.util.MessageTag;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.meta.CommandMeta;

import java.util.List;
import java.util.function.Predicate;

@Singleton
public class HelperModule extends AbstractModuleCommand<Localization.Command.Helper> {

    @Getter private final Command.Helper command;
    private final Permission.Command.Helper permission;

    private final FPlayerService fPlayerService;
    private final ProxySender proxySender;
    private final PermissionChecker permissionChecker;
    private final CommandRegistry commandRegistry;

    @Inject
    public HelperModule(FileResolver fileResolver,
                        FPlayerService fPlayerService,
                        ProxySender proxySender,
                        PermissionChecker permissionChecker,
                        CommandRegistry commandRegistry) {
        super(localization -> localization.getCommand().getHelper(), null);

        this.fPlayerService = fPlayerService;
        this.proxySender = proxySender;
        this.permissionChecker = permissionChecker;
        this.commandRegistry = commandRegistry;

        command = fileResolver.getCommand().getHelper();
        permission = fileResolver.getPermission().getCommand().getHelper();

        addPredicate(this::checkCooldown);
        addPredicate(fPlayer -> checkDisable(fPlayer, fPlayer, DisableAction.YOU));
        addPredicate(this::checkMute);
    }

    @Override
    protected boolean isConfigEnable() {
        return command.isEnable();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        registerPermission(permission.getSee());

        String commandName = getName(command);
        String promptMessage = getPrompt().getMessage();
        commandRegistry.registerCommand(manager ->
                manager.commandBuilder(commandName, command.getAliases(), CommandMeta.empty())
                        .permission(permission.getName())
                        .required(promptMessage, commandRegistry.nativeMessageParser())
                        .handler(this)
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (checkModulePredicates(fPlayer)) return;

        Predicate<FPlayer> filter = getFilterSee();

        List<FPlayer> recipients = fPlayerService.getFPlayers().stream().filter(filter).toList();
        if (recipients.isEmpty()) {
            boolean nullHelper = !proxySender.isEnable() || fPlayerService.findOnlineFPlayers().stream()
                    .noneMatch(online -> permissionChecker.check(online, permission.getSee()));

            if (nullHelper) {
                builder(fPlayer)
                        .format(Localization.Command.Helper::getNullHelper)
                        .sendBuilt();
                return;
            }
        }

        String promptMessage = getPrompt().getMessage();
        String message = commandContext.get(promptMessage);

        builder(fPlayer)
                .destination(command.getDestination())
                .format(Localization.Command.Helper::getPlayer)
                .sendBuilt();

        builder(fPlayer)
                .destination(command.getDestination())
                .range(command.getRange())
                .filter(filter)
                .tag(MessageTag.COMMAND_HELPER)
                .format(Localization.Command.Helper::getGlobal)
                .message(message)
                .proxy(output -> output.writeUTF(message))
                .integration(s -> s.replace("<message>", message))
                .sound(getSound())
                .sendBuilt();
    }

    public Predicate<FPlayer> getFilterSee() {
        return fPlayer -> permissionChecker.check(fPlayer, permission.getSee());
    }
}
