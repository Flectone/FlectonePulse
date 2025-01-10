package net.flectone.pulse.module.command.online;

import lombok.Getter;
import net.flectone.pulse.database.Database;
import net.flectone.pulse.file.Command;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.util.CommandUtil;
import net.flectone.pulse.util.DisableAction;

import java.sql.SQLException;
import java.util.function.BiFunction;

public abstract class OnlineModule extends AbstractModuleCommand<Localization.Command.Online> {

    @Getter private final Command.Online command;
    @Getter private final Permission.Command.Online permission;

    private final CommandUtil commandUtil;

    public OnlineModule(FileManager fileManager,
                        CommandUtil commandUtil) {
        super(localization -> localization.getCommand().getOnline(), null);

        this.commandUtil = commandUtil;

        command = fileManager.getCommand().getOnline();
        permission = fileManager.getPermission().getCommand().getOnline();

        addPredicate(this::checkCooldown);
        addPredicate(fPlayer -> checkDisable(fPlayer, fPlayer, DisableAction.YOU));
    }

    @Override
    public void onCommand(Database database, FPlayer fPlayer, Object arguments) throws SQLException {
        if (checkModulePredicates(fPlayer)) return;

        String target = commandUtil.getString(1, arguments);

        FPlayer targetFPlayer = database.getFPlayer(target);
        if (targetFPlayer.isUnknown()) {
            builder(fPlayer)
                    .format(Localization.Command.Online::getNullPlayer)
                    .sendBuilt();
            return;
        }

        builder(targetFPlayer)
                .destination(command.getDestination())
                .receiver(fPlayer)
                .format(getResolver(fPlayer, targetFPlayer, commandUtil.getLiteral(0, arguments)))
                .sound(getSound())
                .sendBuilt();
    }

    public abstract BiFunction<FPlayer, Localization.Command.Online, String> getResolver(FPlayer fPlayer, FPlayer targetFPlayer, String argument);

    @Override
    public void reload() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        getCommand().getAliases().forEach(commandUtil::unregister);

        createCommand();
    }

    @Override
    public boolean isConfigEnable() {
        return command.isEnable();
    }
}
