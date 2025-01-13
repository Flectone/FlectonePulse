package net.flectone.pulse.module.command.unmute;

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

public abstract class UnmuteModule extends AbstractModuleCommand<Localization.Command.Unmute> {

    @Getter private final Command.Unmute command;
    @Getter private final Permission.Command.Unmute permission;

    private final ThreadManager threadManager;
    private final CommandUtil commandUtil;

    public UnmuteModule(FileManager fileManager,
                        ThreadManager threadManager,
                        CommandUtil commandUtil) {
        super(localization -> localization.getCommand().getUnmute(), null);

        this.threadManager = threadManager;
        this.commandUtil = commandUtil;

        command = fileManager.getCommand().getUnmute();
        permission = fileManager.getPermission().getCommand().getUnmute();
    }

    @Override
    public void onCommand(FPlayer fPlayer, Object arguments) {
        if (checkModulePredicates(fPlayer)) return;
        if (checkCooldown(fPlayer)) return;

        String target = commandUtil.getString(0, arguments);
        int id = commandUtil.getByClassOrDefault(1, Integer.class, -1, arguments);

        unmute(fPlayer, target, id);
    }

    public void unmute(FPlayer fPlayer, String target, int id) {
        if (checkModulePredicates(fPlayer)) return;

        threadManager.runDatabase(database -> {
            FPlayer fTarget = database.getFPlayer(target);
            if (fTarget.isUnknown()) {
                builder(fPlayer)
                        .format(Localization.Command.Unmute::getNullPlayer)
                        .sendBuilt();
                return;
            }

            List<Moderation> mutes = new ArrayList<>();

            if (id == -1) {
                mutes.addAll(database.getValidModerations(fTarget, Moderation.Type.MUTE));
            } else {
                database.getValidModerations(fTarget, Moderation.Type.MUTE).stream()
                        .filter(moderation -> moderation.getId() == id)
                        .findAny()
                        .ifPresent(mutes::add);
            }

            if (mutes.isEmpty()) {
                builder(fPlayer)
                        .format(Localization.Command.Unmute::getNotMuted)
                        .sendBuilt();
                return;
            }

            for (Moderation mute : mutes) {
                database.setInvalidModeration(mute);
            }

            builder(fTarget)
                    .tag(MessageTag.COMMAND_UNMUTE)
                    .destination(command.getDestination())
                    .range(command.getRange())
                    .filter(filter -> filter.is(FPlayer.Setting.MUTE))
                    .format(Localization.Command.Unmute::getFormat)
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
