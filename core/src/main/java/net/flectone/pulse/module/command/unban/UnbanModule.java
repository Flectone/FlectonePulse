package net.flectone.pulse.module.command.unban;

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
import net.flectone.pulse.util.MessageTag;

import java.util.ArrayList;
import java.util.List;

public abstract class UnbanModule extends AbstractModuleCommand<Localization.Command.Unban> {

    @Getter private final Command.Unban command;
    @Getter private final Permission.Command.Unban permission;

    private final ThreadManager threadManager;
    private final CommandUtil commandUtil;

    public UnbanModule(FileManager fileManager,
                       ThreadManager threadManager,
                       CommandUtil commandUtil) {
        super(localization -> localization.getCommand().getUnban(), null);

        this.threadManager = threadManager;
        this.commandUtil = commandUtil;

        command = fileManager.getCommand().getUnban();
        permission = fileManager.getPermission().getCommand().getUnban();
    }

    @Override
    public void onCommand(FPlayer fPlayer, Object arguments) {
        if (checkModulePredicates(fPlayer)) return;
        if (checkCooldown(fPlayer)) return;

        String target = commandUtil.getString(0, arguments);
        int id = commandUtil.getByClassOrDefault(1, Integer.class, -1, arguments);

        unban(fPlayer, target, id);
    }

    public void unban(FPlayer fPlayer, String target, int id) {
        if (checkModulePredicates(fPlayer)) return;

        threadManager.runDatabase(database -> {
            FPlayer fTarget = database.getFPlayer(target);
            if (fTarget.isUnknown()) {
                builder(fPlayer)
                        .format(Localization.Command.Unban::getNullPlayer)
                        .sendBuilt();
                return;
            }

            List<Moderation> bans = new ArrayList<>();

            if (id == -1) {
                bans.addAll(database.getValidModerations(fTarget, Moderation.Type.BAN));
            } else {
                database.getValidModerations(fTarget, Moderation.Type.BAN).stream()
                        .filter(moderation -> moderation.getId() == id)
                        .findAny()
                        .ifPresent(bans::add);
            }

            if (bans.isEmpty()) {
                builder(fPlayer)
                        .format(Localization.Command.Unban::getNotBanned)
                        .sendBuilt();
                return;
            }

            for (Moderation ban : bans) {
                database.setInvalidModeration(ban);
            }

            builder(fTarget)
                    .tag(MessageTag.COMMAND_UNBAN)
                    .destination(command.getDestination())
                    .range(command.getRange())
                    .filter(filter -> filter.is(FPlayer.Setting.BAN))
                    .format(Localization.Command.Unban::getFormat)
                    .proxy()
                    .integration()
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
