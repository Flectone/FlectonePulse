package net.flectone.pulse.module.command.helper;

import lombok.Getter;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.connector.ProxyConnector;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.CommandUtil;
import net.flectone.pulse.util.DisableAction;
import net.flectone.pulse.util.MessageTag;
import net.flectone.pulse.util.PermissionUtil;

import java.util.List;
import java.util.function.Predicate;

public abstract class HelperModule extends AbstractModuleCommand<Localization.Command.Helper> {

    @Getter private final Command.Helper command;
    @Getter private final Permission.Command.Helper permission;

    private final FPlayerService fPlayerService;
    private final ProxyConnector proxyConnector;
    private final PermissionUtil permissionUtil;
    private final CommandUtil commandUtil;

    public HelperModule(FileManager fileManager,
                        FPlayerService fPlayerService,
                        ProxyConnector proxyConnector,
                        PermissionUtil permissionUtil,
                        CommandUtil commandUtil) {
        super(localization -> localization.getCommand().getHelper(), null);

        this.fPlayerService = fPlayerService;
        this.proxyConnector = proxyConnector;
        this.permissionUtil = permissionUtil;
        this.commandUtil = commandUtil;

        command = fileManager.getCommand().getHelper();
        permission = fileManager.getPermission().getCommand().getHelper();

        addPredicate(this::checkCooldown);
        addPredicate(fPlayer -> checkDisable(fPlayer, fPlayer, DisableAction.YOU));
        addPredicate(this::checkMute);
    }

    @Override
    public void onCommand(FPlayer fPlayer, Object arguments) {
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

        String message = commandUtil.getString(0, arguments);

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

    @Override
    public void reload() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        registerPermission(permission.getSee());

        getCommand().getAliases().forEach(commandUtil::unregister);

        createCommand();
    }

    @Override
    public boolean isConfigEnable() {
        return command.isEnable();
    }
}
