package net.flectone.pulse.module.command.helper;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.connector.ProxyConnector;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.registry.CommandRegistry;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.DisableAction;
import net.flectone.pulse.util.MessageTag;
import net.flectone.pulse.util.PermissionUtil;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.meta.CommandMeta;

import java.util.List;
import java.util.function.Predicate;

@Singleton
public class HelperModule extends AbstractModuleCommand<Localization.Command.Helper> {

    @Getter private final Command.Helper command;
    private final Permission.Command.Helper permission;

    private final FPlayerService fPlayerService;
    private final ProxyConnector proxyConnector;
    private final PermissionUtil permissionUtil;
    private final CommandRegistry commandRegistry;

    @Inject
    public HelperModule(FileManager fileManager,
                        FPlayerService fPlayerService,
                        ProxyConnector proxyConnector,
                        PermissionUtil permissionUtil,
                        CommandRegistry commandRegistry) {
        super(localization -> localization.getCommand().getHelper(), null);

        this.fPlayerService = fPlayerService;
        this.proxyConnector = proxyConnector;
        this.permissionUtil = permissionUtil;
        this.commandRegistry = commandRegistry;

        command = fileManager.getCommand().getHelper();
        permission = fileManager.getPermission().getCommand().getHelper();

        addPredicate(this::checkCooldown);
        addPredicate(fPlayer -> checkDisable(fPlayer, fPlayer, DisableAction.YOU));
        addPredicate(this::checkMute);
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
            boolean nullHelper = !proxyConnector.isEnable() || fPlayerService.findOnlineFPlayers().stream()
                    .noneMatch(online -> permissionUtil.has(online, permission.getSee()));

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
        return fPlayer -> permissionUtil.has(fPlayer, permission.getSee());
    }
}
