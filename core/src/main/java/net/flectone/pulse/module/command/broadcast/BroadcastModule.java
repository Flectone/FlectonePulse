package net.flectone.pulse.module.command.broadcast;

import lombok.Getter;
import net.flectone.pulse.file.Command;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.util.CommandUtil;
import net.flectone.pulse.util.DisableAction;
import net.flectone.pulse.util.MessageTag;

public abstract class BroadcastModule extends AbstractModuleCommand<Localization.Command.Broadcast> {

    @Getter
    private final Command.Broadcast command;
    @Getter
    private final Permission.Command.Broadcast permission;

    private final CommandUtil commandUtil;

    public BroadcastModule(FileManager fileManager,
                           CommandUtil commandUtil) {
        super(localization -> localization.getCommand().getBroadcast(), fPlayer -> fPlayer.is(FPlayer.Setting.BROADCAST));

        this.commandUtil = commandUtil;

        command = fileManager.getCommand().getBroadcast();
        permission = fileManager.getPermission().getCommand().getBroadcast();

        addPredicate(this::checkCooldown);
        addPredicate(fPlayer -> checkDisable(fPlayer, fPlayer, DisableAction.YOU));
        addPredicate(this::checkMute);
    }

    @Override
    public void onCommand(FPlayer fPlayer, Object arguments) {
        if (checkModulePredicates(fPlayer)) return;

        String message = commandUtil.getString(0, arguments);
        if (message == null) return;

        builder(fPlayer)
                .range(command.getRange())
                .tag(MessageTag.COMMAND_BROADCAST)
                .format(Localization.Command.Broadcast::getFormat)
                .message((fResolver, s) -> message)
                .proxy(output -> output.writeUTF(message))
                .integration(s -> s.replace("<message>", message))
                .sound(getSound())
                .sendBuilt();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        createCommand();
    }

    @Override
    public boolean isConfigEnable() {
        return command.isEnable();
    }
}
