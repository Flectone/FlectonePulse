package net.flectone.pulse.module.command.unwarn;

import lombok.Getter;
import net.flectone.pulse.file.Command;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.manager.ThreadManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Moderation;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.util.CommandUtil;

import java.util.ArrayList;
import java.util.List;

public abstract class UnwarnModule extends AbstractModuleCommand<Localization.Command.Unwarn> {

    @Getter private final Command.Unwarn command;
    @Getter private final Permission.Command.Unwarn permission;

    private final ThreadManager threadManager;
    private final CommandUtil commandUtil;

    public UnwarnModule(FileManager fileManager,
                        ThreadManager threadManager,
                        CommandUtil commandUtil) {
        super(localization -> localization.getCommand().getUnwarn(), null);

        this.threadManager = threadManager;
        this.commandUtil = commandUtil;

        command = fileManager.getCommand().getUnwarn();
        permission = fileManager.getPermission().getCommand().getUnwarn();
    }

    @Override
    public void onCommand(FPlayer fPlayer, Object arguments) {
        if (checkModulePredicates(fPlayer)) return;
        if (checkCooldown(fPlayer)) return;

        String target = commandUtil.getString(0, arguments);
        int id = commandUtil.getByClassOrDefault(1, Integer.class, -1, arguments);

        unwarn(fPlayer, target, id);
    }

    public void unwarn(FPlayer fPlayer, String target, int id) {
        if (checkModulePredicates(fPlayer)) return;

        threadManager.runDatabase(database -> {
            FPlayer fTarget = database.getFPlayer(target);
            if (fTarget.isUnknown()) {
                builder(fPlayer)
                        .format(Localization.Command.Unwarn::getNullPlayer)
                        .sendBuilt();
                return;
            }

            List<Moderation> warns = new ArrayList<>();

            if (id == -1) {
                warns.addAll(database.getValidModerations(fTarget, Moderation.Type.WARN));
            } else {
                database.getValidModerations(fTarget, Moderation.Type.WARN).stream()
                        .filter(warn -> warn.getId() == id)
                        .findAny()
                        .ifPresent(warns::add);
            }

            if (warns.isEmpty()) {
                builder(fPlayer)
                        .format(Localization.Command.Unwarn::getNotWarned)
                        .sendBuilt();
                return;
            }

            for (Moderation warn : warns) {
                database.setInvalidModeration(warn);
            }

            builder(fTarget)
                    .destination(command.getDestination())
                    .receiver(fPlayer)
                    .format(Localization.Command.Unwarn::getFormat)
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
