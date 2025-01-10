package net.flectone.pulse.module.command.ignore;

import com.google.inject.Inject;
import lombok.Getter;
import net.flectone.pulse.file.Command;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.manager.ThreadManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.ignore.model.Ignore;
import net.flectone.pulse.util.CommandUtil;
import net.flectone.pulse.util.DisableAction;

import java.util.Optional;

public abstract class IgnoreModule extends AbstractModuleCommand<Localization.Command.Ignore> {

    @Getter private final Command.Ignore command;
    @Getter private final Permission.Command.Ignore permission;

    private final ThreadManager threadManager;
    private final CommandUtil commandUtil;

    @Inject
    public IgnoreModule(FileManager fileManager,
                        ThreadManager threadManager,
                        CommandUtil commandUtil) {
        super(localization -> localization.getCommand().getIgnore(), null);

        this.threadManager = threadManager;
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

        threadManager.runDatabase(database -> {
            FPlayer fIgnored = database.getFPlayer(offlinePlayerName);
            if (fIgnored.isUnknown()) {
                builder(fPlayer)
                        .format(Localization.Command.Ignore::getNullPlayer)
                        .sendBuilt();
                return;
            }

            Optional<Ignore> ignore = fPlayer.getIgnores().stream().filter(i -> i.target() == fIgnored.getId()).findFirst();

            if (ignore.isPresent()) {
                fPlayer.getIgnores().remove(ignore.get());
                database.removeIgnore(ignore.get());
            } else {
                Ignore newIgnore = database.insertIgnore(fPlayer, fIgnored);
                if (newIgnore == null) return;
                fPlayer.getIgnores().add(newIgnore);
            }

            builder(fIgnored)
                    .destination(command.getDestination())
                    .receiver(fPlayer)
                    .format(s -> ignore.isEmpty() ? s.getFormatTrue() : s.getFormatFalse())
                    .sound(getSound())
                    .sendBuilt();
        });
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
