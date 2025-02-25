package net.flectone.pulse.module.command.ignore;

import com.google.inject.Inject;
import lombok.Getter;
import net.flectone.pulse.database.dao.FPlayerDAO;
import net.flectone.pulse.database.dao.IgnoreDAO;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.ignore.model.Ignore;
import net.flectone.pulse.util.CommandUtil;
import net.flectone.pulse.util.DisableAction;

import java.util.Optional;

public abstract class IgnoreModule extends AbstractModuleCommand<Localization.Command.Ignore> {

    @Getter private final Command.Ignore command;
    @Getter private final Permission.Command.Ignore permission;

    private final FPlayerDAO fPlayerDAO;
    private final IgnoreDAO ignoreDAO;
    private final CommandUtil commandUtil;

    @Inject
    public IgnoreModule(FileManager fileManager,
                        FPlayerDAO fPlayerDAO,
                        IgnoreDAO ignoreDAO,
                        CommandUtil commandUtil) {
        super(localization -> localization.getCommand().getIgnore(), null);

        this.fPlayerDAO = fPlayerDAO;
        this.ignoreDAO = ignoreDAO;
        this.commandUtil = commandUtil;

        command = fileManager.getCommand().getIgnore();
        permission = fileManager.getPermission().getCommand().getIgnore();

        addPredicate(this::checkCooldown);
        addPredicate(fPlayer -> checkDisable(fPlayer, fPlayer, DisableAction.YOU));
    }

    @Override
    public void onCommand(FPlayer fPlayer, Object arguments) {
        if (checkCooldown(fPlayer)) return;
        if (checkDisable(fPlayer, fPlayer, DisableAction.YOU)) return;
        if (checkModulePredicates(fPlayer)) return;

        String target = commandUtil.getString(0, arguments);

        ignore(fPlayer, target);
    }

    public void ignore(FPlayer fPlayer, String offlinePlayerName) {
        if (checkModulePredicates(fPlayer)) return;

        if (fPlayer.getName().equalsIgnoreCase(offlinePlayerName)) {
            builder(fPlayer)
                    .format(Localization.Command.Ignore::getMyself)
                    .sendBuilt();
            return;
        }

        FPlayer fIgnored = fPlayerDAO.getFPlayer(offlinePlayerName);
        if (fIgnored.isUnknown()) {
            builder(fPlayer)
                    .format(Localization.Command.Ignore::getNullPlayer)
                    .sendBuilt();
            return;
        }

        Optional<Ignore> ignore = fPlayer.getIgnores().stream().filter(i -> i.target() == fIgnored.getId()).findFirst();

        if (ignore.isPresent()) {
            fPlayer.getIgnores().remove(ignore.get());
            ignoreDAO.delete(ignore.get());
        } else {
            Ignore newIgnore = ignoreDAO.insert(fPlayer, fIgnored);
            if (newIgnore == null) return;
            fPlayer.getIgnores().add(newIgnore);
        }

        builder(fIgnored)
                .destination(command.getDestination())
                .receiver(fPlayer)
                .format(s -> ignore.isEmpty() ? s.getFormatTrue() : s.getFormatFalse())
                .sound(getSound())
                .sendBuilt();
    }

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
